package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.dtos.PlatformDto
import io.github.gerardorodriguezdev.chamaleon.core.extractors.EnvironmentNameExtractor
import io.github.gerardorodriguezdev.chamaleon.core.mappers.PlatformMapperImpl
import io.github.gerardorodriguezdev.chamaleon.core.matchers.EnvironmentFileNameMatcher
import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsParserResult
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsParserResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsParserResult.Success
import kotlinx.serialization.json.Json
import java.io.File

public interface EnvironmentsParser {
    public fun parse(environmentsDirectory: File): EnvironmentsParserResult
}

internal class DefaultEnvironmentsParser(
    private val environmentFileMatcher: EnvironmentFileNameMatcher,
    private val environmentNameExtractor: EnvironmentNameExtractor,
) : EnvironmentsParser {

    override fun parse(environmentsDirectory: File): EnvironmentsParserResult {
        try {
            val environmentsDirectoryFiles = environmentsDirectory.listFiles()
            val environmentsFiles = environmentsDirectoryFiles.filter { file -> environmentFileMatcher(file) }

            val environments = environmentsFiles.map { environmentFile ->
                val environmentName = environmentNameExtractor(environmentFile)
                if (environmentName.isEmpty()) {
                    return Failure.EnvironmentNameEmpty(
                        environmentsDirectoryPath = environmentsDirectory.path,
                        environmentFilePath = environmentFile.path
                    )
                }

                val fileContent = environmentFile.readText()
                if (fileContent.isEmpty()) {
                    return Failure.FileIsEmpty(
                        environmentsDirectoryPath = environmentsDirectory.path,
                        environmentFilePath = environmentFile.path
                    )
                }

                val platformDtos = Json.decodeFromString<Set<PlatformDto>>(fileContent)
                val platformsMap = platformDtos.toPlatforms().associateBy { platform -> platform.platformType }

                Environment(name = environmentName, platformsMap = platformsMap)
            }

            val environmentsMap = environments.associateBy { environment -> environment.name }
            return Success(environmentsMap)
        } catch (error: Exception) {
            return Failure.Serialization(
                environmentsDirectoryPath = environmentsDirectory.path,
                throwable = error
            )
        }
    }

    private fun Set<PlatformDto>.toPlatforms(): List<Platform> = map { PlatformMapperImpl.toModel(it) }
}