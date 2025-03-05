package io.github.gerardorodriguezdev.chamaleon.core.serializers

import arrow.core.Either
import arrow.core.raise.catch
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.right
import io.github.gerardorodriguezdev.chamaleon.core.extractors.DefaultEnvironmentFileNameExtractor
import io.github.gerardorodriguezdev.chamaleon.core.extractors.EnvironmentFileNameExtractor
import io.github.gerardorodriguezdev.chamaleon.core.models.Project
import io.github.gerardorodriguezdev.chamaleon.core.models.Project.Companion.propertiesExistingFile
import io.github.gerardorodriguezdev.chamaleon.core.models.Project.Companion.schemaExistingFile
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectSerializationResult
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectSerializationResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectSerializationResult.Success
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.utils.PrettyJson
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

public interface ProjectSerializer {
    public suspend fun serialize(project: Project): ProjectSerializationResult

    public companion object {
        public fun create(): ProjectSerializer = DefaultProjectSerializer()
    }
}

internal class DefaultProjectSerializer(
    private val environmentFileNameExtractor: EnvironmentFileNameExtractor = DefaultEnvironmentFileNameExtractor,
) : ProjectSerializer {

    override suspend fun serialize(project: Project): ProjectSerializationResult =
        serializeEither(project).fold(
            ifLeft = { it },
            ifRight = { it }
        )

    private suspend fun serializeEither(project: Project): Either<Failure, Success> =
        either {
            coroutineScope {
                val propertiesSerializationResult = async { project.serializeProperties() }
                val schemaSerializationResult = async { project.serializeSchema() }
                val environmentsSerializationResult = async { project.serializeEnvironments() }

                propertiesSerializationResult.await().bind()
                schemaSerializationResult.await().bind()
                environmentsSerializationResult.await().bind()

                Success
            }
        }

    private fun Project.serializeProperties(): Either<Failure, Unit> =
        either {
            catch(
                block = {
                    val propertiesFile = environmentsDirectory.propertiesExistingFile(createIfNotPresent = true)
                    ensureNotNull(propertiesFile) {
                        Failure.InvalidPropertiesFile(
                            environmentsDirectoryPath = environmentsDirectory.path.value,
                        )
                    }

                    val propertiesFileContent = PrettyJson.encodeToString(properties)
                    propertiesFile.writeContent(propertiesFileContent)
                },
                catch = { error -> error.toSerializationError(environmentsDirectory) }
            )
        }

    private fun Project.serializeSchema(): Either<Failure, Unit> =
        either {
            catch(
                block = {
                    val schemaFile = environmentsDirectory.schemaExistingFile(createIfNotPresent = true)
                    ensureNotNull(schemaFile) {
                        Failure.InvalidSchemaFile(environmentsDirectoryPath = environmentsDirectory.path.value)
                    }

                    val schemaFileContent = PrettyJson.encodeToString(schema)
                    schemaFile.writeContent(schemaFileContent)
                },
                catch = { error -> error.toSerializationError(environmentsDirectory) }
            )
        }

    private suspend fun Project.serializeEnvironments(): Either<Failure, Unit> =
        either {
            if (environments == null) return Unit.right()

            coroutineScope {
                environments
                    .values
                    .map { environment ->
                        async {
                            val environmentFileName = environmentFileNameExtractor(environment.name)
                            val environmentFile = environmentsDirectory.existingFile(
                                fileName = environmentFileName,
                                createIfNotPresent = true
                            )
                            ensureNotNull(environmentFile) {
                                Failure.InvalidEnvironmentFile(
                                    environmentsDirectoryPath = environmentsDirectory.path.value,
                                    environmentFileName = environmentFileName.value,
                                )
                            }

                            catch(
                                block = {
                                    val platformsJson = PrettyJson.encodeToString(environment.platforms)
                                    environmentFile.writeContent(platformsJson)
                                },
                                catch = { error -> error.toSerializationError(environmentsDirectory) }
                            )
                        }
                    }
                    .awaitAll()
            }
        }

    private fun Throwable.toSerializationError(environmentsDirectory: ExistingDirectory): Failure.Serialization =
        Failure.Serialization(
            environmentsDirectoryPath = environmentsDirectory.path.value,
            error = this,
        )
}