package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.extractors.EnvironmentNameExtractor
import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform
import io.github.gerardorodriguezdev.chamaleon.core.models.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.models.Project.Companion.isEnvironmentFileName
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsParserResult
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsParserResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsParserResult.Success
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore.Companion.toNonEmptyKeySetStore
import kotlinx.serialization.json.Json

//TODO: Single parser?
public interface EnvironmentsParser {
    public fun parse(environmentsDirectory: ExistingDirectory): EnvironmentsParserResult
}

internal class DefaultEnvironmentsParser(
    private val environmentNameExtractor: EnvironmentNameExtractor,
) : EnvironmentsParser {

    override fun parse(environmentsDirectory: ExistingDirectory): EnvironmentsParserResult {
        try {
            //TODO: Case where env dir has invalid env files
            //TODO: Sep file?
            val environmentsFiles = environmentsDirectory.existingFiles { fileName -> fileName.isEnvironmentFileName() }

            val environments = environmentsFiles
                .map { environmentFile ->
                    val fileContent = environmentFile.readContent()
                    if (fileContent.isEmpty()) {
                        return Failure.FileIsEmpty(
                            environmentsDirectoryPath = environmentsDirectory.path.value,
                            environmentFilePath = environmentFile.path.value
                        )
                    }

                    val environmentName = environmentNameExtractor(environmentFile)
                    val platforms = Json.decodeFromString<NonEmptyKeySetStore<PlatformType, Platform>>(fileContent)

                    Environment(name = environmentName, platforms = platforms)
                }.toSet()

            val environmentsKeySetStore = environments.toNonEmptyKeySetStore()
            return Success(environmentsKeySetStore)
        } catch (error: Exception) {
            return Failure.Serialization(
                environmentsDirectoryPath = environmentsDirectory.path.value,
                throwable = error
            )
        }
    }
}