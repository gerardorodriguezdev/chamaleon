package io.github.gerardorodriguezdev.chamaleon.core.generators

import io.github.gerardorodriguezdev.chamaleon.core.extractors.EnvironmentFileNameExtractor
import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.models.Project
import io.github.gerardorodriguezdev.chamaleon.core.models.Properties
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema
import io.github.gerardorodriguezdev.chamaleon.core.results.UpdateProjectResult
import io.github.gerardorodriguezdev.chamaleon.core.results.UpdateProjectResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingFile
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore
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

    override suspend fun updateProject(project: Project): UpdateProjectResult {
        val propertiesFile = project.propertiesExistingFile(createIfNotPresent = true)
        if (propertiesFile == null) return Failure.InvalidPropertiesFile(project.environmentsDirectory.path.value)

        val updatePropertiesResult = updateProperties(
            environmentsDirectoryPath = project.environmentsDirectory.path.value,
            propertiesFile = propertiesFile,
            newProperties = project.properties,
        )
        if (updatePropertiesResult is Failure) return updatePropertiesResult

        val schemaFile = project.schemaExistingFile(createIfNotPresent = true)
        if (schemaFile == null) return Failure.InvalidSchemaFile(project.environmentsDirectory.path.value)

        val updateSchemaResult = updateSchema(
            environmentsDirectoryPath = project.environmentsDirectory.path.value,
            schemaFile = schemaFile,
            newSchema = project.schema,
        )
        if (updateSchemaResult is Failure) return updateSchemaResult

        project.environments?.let {
            val updateEnvironmentsResult = updateEnvironments(
                environmentsDirectory = project.environmentsDirectory,
                newEnvironments = project.environments,
            )
            if (updateEnvironmentsResult is Failure) return updateEnvironmentsResult
        }

        return UpdateProjectResult.Success
    }

    private fun updateSchema(environmentsDirectoryPath: String, schemaFile: ExistingFile, newSchema: Schema): Failure? {
        return try {
            val schemaFileContent = PrettyJson.encodeToString(newSchema)
            schemaFile.writeContent(schemaFileContent)

            null
        } catch (error: Exception) {
            Failure.Serialization(environmentsDirectoryPath = environmentsDirectoryPath, throwable = error)
        }
    }

    private fun updateProperties(
        environmentsDirectoryPath: String,
        propertiesFile: ExistingFile,
        newProperties: Properties
    ): Failure? {
        return try {
            val propertiesFileContent = PrettyJson.encodeToString(newProperties)
            propertiesFile.writeContent(propertiesFileContent)

            null
        } catch (error: Exception) {
            return Failure.Serialization(environmentsDirectoryPath = environmentsDirectoryPath, throwable = error)
        }
    }

    private suspend fun updateEnvironments(
        environmentsDirectory: ExistingDirectory,
        newEnvironments: NonEmptyKeySetStore<String, Environment>,
    ): Failure? =
        coroutineScope {
            return@coroutineScope try {
                newEnvironments
                    .map { (_, environment) ->
                        async {
                            val environmentFileName = environmentFileNameExtractor(environment.name)
                            val environmentExistingFile = environmentsDirectory.existingFile(
                                fileName = environmentFileName,
                                createIfNotPresent = true
                            )
                            if (environmentExistingFile == null) {
                                return@async Failure.InvalidEnvironmentFile(
                                    environmentsDirectory.path.value
                                )
                            }

                            val platformsJson = PrettyJson.encodeToString(environment.platforms)
                            environmentExistingFile.writeContent(platformsJson)
                        }
                    }
                    .awaitAll()

                null
            } catch (error: Exception) {
                Failure.Serialization(
                    environmentsDirectoryPath = environmentsDirectory.path.value,
                    throwable = error
                )
            }
        }
}