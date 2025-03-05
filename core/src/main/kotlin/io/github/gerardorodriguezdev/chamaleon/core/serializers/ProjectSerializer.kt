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
        }.fold(
            ifLeft = { it },
            ifRight = { it }
        )

    private fun Project.serializeProperties(): Either<Failure, Unit> =
        either {
            catch(
                block = {
                    val propertiesFile = environmentsDirectory.propertiesExistingFile(createIfNotPresent = true)
                    ensureNotNull(propertiesFile) { Failure.Serialization(environmentsDirectory.path.value) }

                    val propertiesFileContent = PrettyJson.encodeToString(properties)
                    propertiesFile.writeContent(propertiesFileContent)
                },
                catch = { error -> Failure.Serialization(environmentsDirectoryPath = environmentsDirectory.path.value) }
            )
        }

    private fun Project.serializeSchema(): Either<Failure, Unit> =
        either {
            catch(
                block = {
                    val schemaFile = environmentsDirectory.schemaExistingFile(createIfNotPresent = true)
                    ensureNotNull(schemaFile) { Failure.Serialization(environmentsDirectory.path.value) }

                    val schemaFileContent = PrettyJson.encodeToString(schema)
                    schemaFile.writeContent(schemaFileContent)
                },
                catch = { error -> Failure.Serialization(environmentsDirectoryPath = environmentsDirectory.path.value) }
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
                            val environmentExistingFile = environmentsDirectory.existingFile(
                                fileName = environmentFileName,
                                createIfNotPresent = true
                            )
                            ensureNotNull(environmentExistingFile) {
                                Failure.Serialization(environmentsDirectoryPath = environmentsDirectory.path.value)
                            }

                            catch(
                                block = {
                                    val platformsJson = PrettyJson.encodeToString(environment.platforms)
                                    environmentExistingFile.writeContent(platformsJson)
                                },
                                catch = { error -> Failure.Serialization(environmentsDirectoryPath = environmentsDirectory.path.value) }
                            )
                        }
                    }
                    .awaitAll()
            }
        }
}