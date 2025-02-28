package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.isEnvironmentFile
import io.github.gerardorodriguezdev.chamaleon.core.extractors.EnvironmentNameExtractor
import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform
import io.github.gerardorodriguezdev.chamaleon.core.models.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsParserResult
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsParserResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsParserResult.Success
import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.ExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.NonEmptyKeyStore
import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.ValidFile
import kotlinx.serialization.json.Json

public interface EnvironmentsParser {
    public fun parse(environmentsDirectory: ExistingDirectory): EnvironmentsParserResult
}

internal class DefaultEnvironmentsParser(
    private val environmentNameExtractor: EnvironmentNameExtractor,
) : EnvironmentsParser {

    override fun parse(environmentsDirectory: ExistingDirectory): EnvironmentsParserResult {
        try {
            val environmentsDirectoryFiles = environmentsDirectory.directory.listFiles()
            val environmentsFiles = environmentsDirectoryFiles
                .filter { file -> file.isEnvironmentFile() }
                .map { file ->
                    val validFile = ValidFile.of(file)
                    validFile ?: return Failure.InvalidEnvironmentFile(environmentsDirectory.directory.path, file.path)
                }
                .mapNotNull { file -> file.toExistingFile() }

            val environments = environmentsFiles
                .map { environmentFile ->
                    val fileContent = environmentFile.file.readText()
                    if (fileContent.isEmpty()) {
                        return Failure.FileIsEmpty(
                            environmentsDirectoryPath = environmentsDirectory.directory.path,
                            environmentFilePath = environmentFile.file.path
                        )
                    }

                    val environmentName = environmentNameExtractor(environmentFile)
                    val platforms = Json.decodeFromString<NonEmptyKeyStore<PlatformType, Platform>>(fileContent)

                    Environment(name = environmentName, platforms = platforms)
                }.toSet()

            val environmentsKeyStore = NonEmptyKeyStore.of(environments)
            return Success(environmentsKeyStore)
        } catch (error: Exception) {
            return Failure.Serialization(
                environmentsDirectoryPath = environmentsDirectory.directory.path,
                throwable = error
            )
        }
    }
}