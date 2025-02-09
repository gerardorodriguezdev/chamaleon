package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.dtos.PlatformDto
import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Platform
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.AddEnvironmentsResult
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.EnvironmentsParserResult
import io.github.gerardorodriguezdev.chamaleon.core.utils.PrettyJson
import kotlinx.serialization.json.Json
import java.io.File

public interface EnvironmentsParser {
    public fun environmentsParserResult(environmentsDirectory: File): EnvironmentsParserResult
    public fun addEnvironments(
        environmentsDirectory: File,
        environments: Set<Environment>,
    ): AddEnvironmentsResult

    public fun isEnvironmentValid(environment: Environment): Boolean
}

internal class DefaultEnvironmentsParser(
    val environmentFileMatcher: (File) -> Boolean,
    val environmentNameExtractor: (File) -> String,
    val environmentFileNameExtractor: (environmentName: String) -> String,
) : EnvironmentsParser {

    @Suppress("ReturnCount", "TooGenericExceptionCaught")
    override fun environmentsParserResult(environmentsDirectory: File): EnvironmentsParserResult {
        try {
            val environmentsDirectoryFiles = environmentsDirectory.listFiles()
            val environmentsFiles = environmentsDirectoryFiles.filter { file -> environmentFileMatcher(file) }

            val environments = environmentsFiles.map { environmentFile ->
                val environmentName = environmentNameExtractor(environmentFile)
                if (environmentName.isEmpty()) {
                    return EnvironmentsParserResult.Failure.EnvironmentNameEmpty(environmentFile.path)
                }

                val fileContent = environmentFile.readText()
                if (fileContent.isEmpty()) {
                    return EnvironmentsParserResult.Failure.InvalidEnvironment(environmentFile.path)
                }

                val platformDtos = Json.decodeFromString<Set<PlatformDto>>(fileContent)

                Environment(
                    name = environmentName,
                    platforms = platformDtos.toPlatforms(),
                )
            }

            return EnvironmentsParserResult.Success(environments = environments.toSet())
        } catch (error: Exception) {
            return EnvironmentsParserResult.Failure.Serialization(error)
        }
    }

    @Suppress("ReturnCount", "TooGenericExceptionCaught")
    override fun addEnvironments(
        environmentsDirectory: File,
        environments: Set<Environment>,
    ): AddEnvironmentsResult {
        return try {
            if (!environmentsDirectory.isDirectory) {
                return AddEnvironmentsResult.Failure.InvalidDirectory(environmentsDirectory.path)
            }
            if (environments.isEmpty()) {
                return AddEnvironmentsResult.Failure.EmptyEnvironments(environmentsDirectory.path)
            }

            environments.forEach { environment ->
                val validationResult = environment.validationResult(environmentsDirectory.path)
                if (validationResult != null) return validationResult

                val environmentFileName = environmentFileNameExtractor(environment.name)
                val environmentFile = File(environmentsDirectory, environmentFileName)

                if (environmentFile.exists()) {
                    return AddEnvironmentsResult.Failure.FileAlreadyPresent(
                        environmentFile.path
                    )
                }
                environmentFile.createNewFile()

                val platformDtos = environment.platforms.toPlatformDtos()
                val platformDtosJson = PrettyJson.encodeToString(platformDtos)
                environmentFile.writeText(platformDtosJson)
            }

            AddEnvironmentsResult.Success
        } catch (error: Exception) {
            AddEnvironmentsResult.Failure.Serialization(error)
        }
    }

    private fun Environment.validationResult(path: String): AddEnvironmentsResult.Failure? =
        when (isValid()) {
            Environment.ValidationResult.VALID -> null
            Environment.ValidationResult.EMPTY_PLATFORMS -> AddEnvironmentsResult.Failure.EmptyPlatforms(path)
            Environment.ValidationResult.INVALID_PLATFORM -> AddEnvironmentsResult.Failure.InvalidPlatforms(path)
            Environment.ValidationResult.EMPTY_NAME -> AddEnvironmentsResult.Failure.EmptyEnvironmentName(path)
        }

    override fun isEnvironmentValid(environment: Environment): Boolean =
        environment.isValid() == Environment.ValidationResult.VALID

    private fun Set<PlatformDto>.toPlatforms(): Set<Platform> =
        map { platformDto ->
            Platform(
                platformType = platformDto.platformType,
                properties = platformDto.properties.toProperties(),
            )
        }.toSet()

    private fun Set<PlatformDto.PropertyDto>.toProperties(): Set<Platform.Property> =
        map { propertyDto ->
            Platform.Property(
                name = propertyDto.name,
                value = propertyDto.value,
            )
        }.toSet()

    private fun Set<Platform>.toPlatformDtos(): Set<PlatformDto> =
        map { platform -> platform.toPlatformDto() }.toSet()

    private fun Platform.toPlatformDto(): PlatformDto =
        PlatformDto(
            platformType = platformType,
            properties = properties.toPropertyDtos()
        )

    private fun Set<Platform.Property>.toPropertyDtos(): Set<PlatformDto.PropertyDto> =
        mapNotNull { property -> property.toPropertyDto() }.toSet()

    private fun Platform.Property.toPropertyDto(): PlatformDto.PropertyDto? {
        return PlatformDto.PropertyDto(
            name = name,
            value = value ?: return null,
        )
    }
}