package io.github.gerardorodriguezdev.chamaleon.core.serializers

import arrow.core.Either
import arrow.core.raise.catch
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.right
import io.github.gerardorodriguezdev.chamaleon.core.extractors.EnvironmentFileNameExtractor
import io.github.gerardorodriguezdev.chamaleon.core.models.Project
import io.github.gerardorodriguezdev.chamaleon.core.models.Project.Companion.propertiesExistingFile
import io.github.gerardorodriguezdev.chamaleon.core.models.Project.Companion.schemaExistingFile
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectSerializationResult
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectSerializationResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.utils.PrettyJson
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

public interface ProjectSerializer {
    public suspend fun serialize(project: Project): ProjectSerializationResult
}

internal class DefaultProjectSerializer(
    private val environmentFileNameExtractor: EnvironmentFileNameExtractor,
) : ProjectSerializer {

    override suspend fun serialize(project: Project): ProjectSerializationResult =
        either {
            catch(
                block = {
                    coroutineScope {
                        val propertiesSerializationResult = async { project.serializeProperties() }
                        val schemaSerializationResult = async { project.serializeSchema() }
                        val environmentsSerializationResult = async { project.serializeEnvironments() }

                        propertiesSerializationResult.await().bind()
                        schemaSerializationResult.await().bind()
                        environmentsSerializationResult.await().bind()

                        ProjectSerializationResult.Success
                    }
                },
                catch = { error ->
                    Failure.Serialization(
                        environmentsDirectoryPath = project.environmentsDirectory.path.value,
                        throwable = error
                    )
                },
            )
        }.fold(
            ifLeft = { it },
            ifRight = { it }
        )

    private fun Project.serializeProperties(): Either<Failure, Unit> =
        either {
            val propertiesFile = environmentsDirectory.propertiesExistingFile(createIfNotPresent = true)
            ensureNotNull(propertiesFile) { Failure.InvalidPropertiesFile(environmentsDirectory.path.value) }

            val propertiesFileContent = PrettyJson.encodeToString(properties)
            propertiesFile.writeContent(propertiesFileContent)
        }

    private fun Project.serializeSchema(): Either<Failure, Unit> =
        either {
            val schemaFile = environmentsDirectory.schemaExistingFile(createIfNotPresent = true)
            ensureNotNull(schemaFile) { Failure.InvalidSchemaFile(environmentsDirectory.path.value) }

            val schemaFileContent = PrettyJson.encodeToString(schema)
            schemaFile.writeContent(schemaFileContent)
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
                                Failure.InvalidEnvironmentFile(
                                    environmentsDirectoryPath = environmentsDirectory.path.value,
                                    environmentName = environment.name.value,
                                )
                            }

                            val platformsJson = PrettyJson.encodeToString(environment.platforms)
                            environmentExistingFile.writeContent(platformsJson)
                        }
                    }
                    .awaitAll()
            }
        }
}