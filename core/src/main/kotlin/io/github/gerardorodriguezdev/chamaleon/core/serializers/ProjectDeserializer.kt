package io.github.gerardorodriguezdev.chamaleon.core.serializers

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.catch
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.right
import io.github.gerardorodriguezdev.chamaleon.core.extractors.DefaultEnvironmentNameExtractor
import io.github.gerardorodriguezdev.chamaleon.core.extractors.EnvironmentNameExtractor
import io.github.gerardorodriguezdev.chamaleon.core.models.*
import io.github.gerardorodriguezdev.chamaleon.core.models.Project.Companion.isEnvironmentFileName
import io.github.gerardorodriguezdev.chamaleon.core.models.Project.Companion.isEnvironmentsDirectory
import io.github.gerardorodriguezdev.chamaleon.core.models.Project.Companion.projectOf
import io.github.gerardorodriguezdev.chamaleon.core.models.Project.Companion.propertiesExistingFile
import io.github.gerardorodriguezdev.chamaleon.core.models.Project.Companion.schemaExistingFile
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectDeserializationResult
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectDeserializationResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectDeserializationResult.Success
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectValidationResult
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore.Companion.toNonEmptyKeySetStore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json

public interface ProjectDeserializer {
    public suspend fun deserialize(environmentsDirectory: ExistingDirectory): ProjectDeserializationResult
    public suspend fun deserializeRecursively(rootDirectory: ExistingDirectory): List<ProjectDeserializationResult>

    public companion object {
        public fun create(): ProjectDeserializer = DefaultProjectDeserializer()
    }
}

internal class DefaultProjectDeserializer(
    private val environmentNameExtractor: EnvironmentNameExtractor = DefaultEnvironmentNameExtractor,
) : ProjectDeserializer {

    override suspend fun deserialize(environmentsDirectory: ExistingDirectory): ProjectDeserializationResult =
        deserializeEither(environmentsDirectory)
            .fold(
                ifLeft = { it },
                ifRight = { it }
            )

    private suspend fun deserializeEither(environmentsDirectory: ExistingDirectory): Either<Failure, Success> =
        either {
            coroutineScope {
                val propertiesDeserializationResult = async { environmentsDirectory.propertiesDeserialization() }
                val schemaDeserializationResult = async { environmentsDirectory.schemaDeserialization() }
                val environmentsDeserializationResult = async { environmentsDirectory.environmentsDeserialization() }

                val project = project(
                    environmentsDirectory = environmentsDirectory,
                    properties = propertiesDeserializationResult.await().bind(),
                    schema = schemaDeserializationResult.await().bind(),
                    environments = environmentsDeserializationResult.await().bind(),
                ).bind()

                Success(project = project)
            }
        }

    private fun ExistingDirectory.propertiesDeserialization(): Either<Failure, Properties> =
        either {
            val propertiesFile = propertiesExistingFile()
            if (propertiesFile == null) return Properties().right()

            catch(
                block = {
                    val propertiesFileContent = propertiesFile.readContent()
                    if (propertiesFileContent.isEmpty()) return Properties().right()

                    Json.decodeFromString<Properties>(propertiesFileContent)
                },
                catch = { error -> raise(Failure.Deserialization(path.value)) }
            )
        }

    private fun ExistingDirectory.schemaDeserialization(): Either<Failure, Schema> =
        either {
            val schemaFile = schemaExistingFile()
            ensureNotNull(schemaFile) { Failure.Deserialization(path.value) }

            catch(
                block = {
                    val schemaFileContent = schemaFile.readContent()
                    Json.decodeFromString<Schema>(schemaFileContent)
                },
                catch = { raise(Failure.Deserialization(path.value)) }
            )
        }

    private suspend fun ExistingDirectory.environmentsDeserialization(): Either<Failure, NonEmptyKeySetStore<String, Environment>> =
        either {
            val environmentsFiles = existingFiles { fileName -> fileName.isEnvironmentFileName() }

            coroutineScope {
                environmentsFiles
                    .map { environmentFile ->
                        async {
                            val environmentName = environmentNameExtractor(environmentFile)

                            catch(
                                block = {
                                    val environmentFileContent = environmentFile.readContent()
                                    val platforms = Json.decodeFromString<NonEmptyKeySetStore<PlatformType, Platform>>(
                                        environmentFileContent
                                    )
                                    Environment(name = environmentName, platforms = platforms)
                                },
                                catch = { raise(Failure.Deserialization(path.value)) }
                            )
                        }
                    }
                    .awaitAll()
                    .toSet()
                    .toNonEmptyKeySetStore()
                    ?: raise(Failure.Deserialization(path.value))
            }
        }

    private fun project(
        environmentsDirectory: ExistingDirectory,
        schema: Schema,
        environments: NonEmptyKeySetStore<String, Environment>?,
        properties: Properties,
    ): Either<Failure, Project> {
        val projectValidationResult = projectOf(
            environmentsDirectory = environmentsDirectory,
            schema = schema,
            environments = environments,
            properties = properties,
        )

        return when (projectValidationResult) {
            is ProjectValidationResult.Success -> projectValidationResult.project.right()
            is ProjectValidationResult.Failure -> Failure.ProjectValidation(
                environmentsDirectoryPath = environmentsDirectory.path.value,
                error = projectValidationResult
            ).left()
        }
    }

    override suspend fun deserializeRecursively(rootDirectory: ExistingDirectory): List<ProjectDeserializationResult> {
        val environmentsDirectories =
            rootDirectory.existingDirectories { directoryName -> directoryName.isEnvironmentsDirectory() }

        return coroutineScope {
            environmentsDirectories
                .map { environmentsDirectory ->
                    async {
                        deserializeEither(environmentsDirectory = environmentsDirectory)
                            .fold(
                                ifLeft = { it },
                                ifRight = { it },
                            )
                    }
                }
                .awaitAll()
        }
    }
}