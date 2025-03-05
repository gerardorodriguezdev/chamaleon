package io.github.gerardorodriguezdev.chamaleon.core.updaters

import arrow.core.Either
import arrow.core.raise.catch
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.right
import io.github.gerardorodriguezdev.chamaleon.core.extractors.EnvironmentFileNameExtractor
import io.github.gerardorodriguezdev.chamaleon.core.models.Project
import io.github.gerardorodriguezdev.chamaleon.core.results.UpdateProjectResult
import io.github.gerardorodriguezdev.chamaleon.core.results.UpdateProjectResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.utils.PrettyJson
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

public interface ProjectUpdater {
    public suspend fun updateProject(project: Project): UpdateProjectResult
}

internal class DefaultProjectUpdater(
    private val environmentFileNameExtractor: EnvironmentFileNameExtractor,
) : ProjectUpdater {

    override suspend fun updateProject(project: Project): UpdateProjectResult =
        either {
            catch(
                block = {
                    coroutineScope {
                        val updatePropertiesResult = async { project.updateProperties() }
                        val updateSchemaResult = async { project.updateSchema() }
                        val updateEnvironmentsResult = async { project.updateEnvironments() }

                        updatePropertiesResult.await().bind()
                        updateSchemaResult.await().bind()
                        updateEnvironmentsResult.await().bind()

                        UpdateProjectResult.Success
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

    private fun Project.updateProperties(): Either<Failure, Unit> =
        either {
            val propertiesFile = propertiesExistingFile(createIfNotPresent = true)
            ensureNotNull(propertiesFile) { Failure.InvalidPropertiesFile(environmentsDirectory.path.value) }

            val propertiesFileContent = PrettyJson.encodeToString(properties)
            propertiesFile.writeContent(propertiesFileContent)
        }

    private fun Project.updateSchema(): Either<Failure, Unit> =
        either {
            val schemaFile = schemaExistingFile(createIfNotPresent = true)
            ensureNotNull(schemaFile) { Failure.InvalidSchemaFile(environmentsDirectory.path.value) }

            val schemaFileContent = PrettyJson.encodeToString(schema)
            schemaFile.writeContent(schemaFileContent)
        }

    private suspend fun Project.updateEnvironments(): Either<Failure, Unit> =
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
                                Failure.InvalidEnvironmentFile(environmentsDirectory.path.value)
                            }

                            val platformsJson = PrettyJson.encodeToString(environment.platforms)
                            environmentExistingFile.writeContent(platformsJson)
                        }
                    }
                    .awaitAll()
            }
        }
}