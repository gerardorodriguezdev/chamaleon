package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.dtos.PlatformDto
import io.github.gerardorodriguezdev.chamaleon.core.dtos.PlatformDto.PropertyDto
import io.github.gerardorodriguezdev.chamaleon.core.extractors.EnvironmentFileNameExtractor
import io.github.gerardorodriguezdev.chamaleon.core.extractors.EnvironmentNameExtractor
import io.github.gerardorodriguezdev.chamaleon.core.matchers.EnvironmentFileNameMatcher
import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform.Property
import io.github.gerardorodriguezdev.chamaleon.core.results.AddEnvironmentsResult
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsParserResult
import io.github.gerardorodriguezdev.chamaleon.core.utils.PrettyJson
import kotlinx.serialization.json.Json
import java.io.File

public interface EnvironmentsParser {
    public fun environmentsParserResult(environmentsDirectory: File): EnvironmentsParserResult
    public fun addEnvironments(
        environmentsDirectory: File,
        newEnvironments: Set<Environment>,
    ): AddEnvironmentsResult
}

internal class DefaultEnvironmentsParser(
    val environmentFileMatcher: EnvironmentFileNameMatcher,
    val environmentNameExtractor: EnvironmentNameExtractor,
    val environmentFileNameExtractor: EnvironmentFileNameExtractor,
) : EnvironmentsParser {

    override fun environmentsParserResult(environmentsDirectory: File): EnvironmentsParserResult {
        try {
            val environmentsDirectoryFiles = environmentsDirectory.listFiles()
            val environmentsFiles = environmentsDirectoryFiles.filter { file -> environmentFileMatcher(file) }

            val environments = environmentsFiles.map { environmentFile ->
                val environmentName = environmentNameExtractor(environmentFile)
                if (environmentName.isEmpty()) {
                    return EnvironmentsParserResult.Failure.EnvironmentNameEmpty(
                        environmentsDirectoryPath = environmentsDirectory.path,
                        environmentFilePath = environmentFile.path
                    )
                }

                val fileContent = environmentFile.readText()
                if (fileContent.isEmpty()) {
                    return EnvironmentsParserResult.Failure.FileIsEmpty(
                        environmentsDirectoryPath = environmentsDirectory.path,
                        environmentFilePath = environmentFile.path
                    )
                }

                val platformDtos = Json.decodeFromString<Set<PlatformDto>>(fileContent)

                Environment(name = environmentName, platforms = platformDtos.toPlatforms())
            }

            return EnvironmentsParserResult.Success(environments.toSet())
        } catch (error: Exception) {
            return EnvironmentsParserResult.Failure.Serialization(
                environmentsDirectoryPath = environmentsDirectory.path,
                throwable = error
            )
        }
    }

    private fun Set<PlatformDto>.toPlatforms(): Set<Platform> =
        map { platformDto ->
            Platform(
                platformDto.platformType,
                platformDto.properties.toProperties(),
            )
        }.toSet()

    private fun Set<PropertyDto>.toProperties(): Set<Property> =
        map { propertyDto ->
            Property(
                name = propertyDto.name,
                value = propertyDto.value,
            )
        }.toSet()

    override fun addEnvironments(
        environmentsDirectory: File,
        newEnvironments: Set<Environment>,
    ): AddEnvironmentsResult {
        return try {
            if (!environmentsDirectory.isDirectory) {
                return AddEnvironmentsResult.Failure.InvalidDirectory(
                    environmentsDirectoryPath = environmentsDirectory.path
                )
            }

            if (newEnvironments.isEmpty()) {
                return AddEnvironmentsResult.Failure.EmptyEnvironments(
                    environmentsDirectoryPath = environmentsDirectory.path
                )
            }

            newEnvironments.forEach { environment ->
                val environmentFileName = environmentFileNameExtractor(environment.name)
                val environmentFile = File(environmentsDirectory, environmentFileName)

                if (environmentFile.exists()) {
                    return AddEnvironmentsResult.Failure.FileAlreadyPresent(
                        environmentsDirectoryPath = environmentFile.path,
                        environmentName = environmentFileName,
                    )
                }

                environmentFile.createNewFile()

                val platformDtos = environment.platforms.toPlatformDtos()
                val platformDtosJson = PrettyJson.encodeToString(platformDtos)
                environmentFile.writeText(platformDtosJson)
            }

            AddEnvironmentsResult.Success
        } catch (error: Exception) {
            AddEnvironmentsResult.Failure.Serialization(
                environmentsDirectoryPath = environmentsDirectory.path,
                throwable = error
            )
        }
    }

    private fun Set<Platform>.toPlatformDtos(): Set<PlatformDto> =
        map { platform ->
            PlatformDto(
                platform.platformType,
                platform.properties.toPropertyDtos()
            )
        }.toSet()

    private fun Set<Property>.toPropertyDtos(): Set<PropertyDto> =
        map { property ->
            PropertyDto(
                name = property.name,
                value = property.value,
            )
        }.toSet()
}