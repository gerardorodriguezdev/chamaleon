package io.github.gerardorodriguezdev.chamaleon.core.generators

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.core.extractors.EnvironmentFileNameExtractor
import io.github.gerardorodriguezdev.chamaleon.core.generators.GenerateProjectResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.models.Project
import io.github.gerardorodriguezdev.chamaleon.core.models.Properties
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema
import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.ExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.ExistingFile
import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.NonEmptyKeyStore
import io.github.gerardorodriguezdev.chamaleon.core.utils.PrettyJson
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

public interface ProjectGenerator {
    public suspend fun updateProject(project: Project): GenerateProjectResult
}

public sealed interface GenerateProjectResult {
    public data object Success : GenerateProjectResult
    public sealed interface Failure : GenerateProjectResult {
        public val environmentsDirectoryPath: String

        public data class InvalidPropertiesFile(override val environmentsDirectoryPath: String) : Failure
        public data class InvalidSchemaFile(override val environmentsDirectoryPath: String) : Failure
        public data class InvalidEnvironmentFile(override val environmentsDirectoryPath: String) : Failure

        public data class Serialization(
            override val environmentsDirectoryPath: String, val throwable: Throwable
        ) : Failure
    }
}

internal class DefaultProjectGenerator(
    private val environmentFileNameExtractor: EnvironmentFileNameExtractor,
) : ProjectGenerator {

    override suspend fun updateProject(project: Project): GenerateProjectResult {
        val propertiesFile = project.propertiesValidFile()?.toExistingFile(createIfNotPresent = true)
        if (propertiesFile == null) return Failure.InvalidPropertiesFile(project.environmentsDirectory.directory.path)

        val updatePropertiesResult = updateProperties(
            environmentsDirectoryPath = project.environmentsDirectory.directory.path,
            propertiesFile = propertiesFile,
            newProperties = project.properties,
        )
        if (updatePropertiesResult is Failure) return updatePropertiesResult

        val schemaFile = project.schemaValidFile()?.toExistingFile(createIfNotPresent = true)
        if (schemaFile == null) return Failure.InvalidSchemaFile(project.environmentsDirectory.directory.path)

        val updateSchemaResult = updateSchema(
            environmentsDirectoryPath = project.environmentsDirectory.directory.path,
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

        return GenerateProjectResult.Success
    }

    private fun updateSchema(environmentsDirectoryPath: String, schemaFile: ExistingFile, newSchema: Schema): Failure? {
        return try {
            val schemaFileContent = PrettyJson.encodeToString(newSchema)
            schemaFile.file.writeText(schemaFileContent)

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
            propertiesFile.file.writeText(propertiesFileContent)

            null
        } catch (error: Exception) {
            return Failure.Serialization(environmentsDirectoryPath = environmentsDirectoryPath, throwable = error)
        }
    }

    private suspend fun updateEnvironments(
        environmentsDirectory: ExistingDirectory,
        newEnvironments: NonEmptyKeyStore<String, Environment>,
    ): Failure? =
        coroutineScope {
            return@coroutineScope try {
                newEnvironments
                    .map { (_, environment) ->
                        async {
                            val environmentFileName = environmentFileNameExtractor(environment.name)
                            val environmentFile =
                                EnvironmentsProcessor.environmentValidFile(environmentsDirectory, environmentFileName)
                            if (environmentFile == null) {
                                return@async Failure.InvalidEnvironmentFile(
                                    environmentsDirectory.directory.path
                                )
                            }

                            val platformsJson = PrettyJson.encodeToString(environment.platforms)
                            val existingFile = environmentFile.toExistingFile(createIfNotPresent = true)
                            existingFile?.file?.writeText(platformsJson)
                        }
                    }
                    .awaitAll()

                null
            } catch (error: Exception) {
                Failure.Serialization(
                    environmentsDirectoryPath = environmentsDirectory.directory.path,
                    throwable = error
                )
            }
        }
}