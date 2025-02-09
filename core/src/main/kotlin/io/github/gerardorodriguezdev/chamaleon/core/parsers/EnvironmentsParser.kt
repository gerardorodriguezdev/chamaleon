package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.dtos.PlatformDto
import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Platform
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.EnvironmentsParserResult
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.EnvironmentsParserResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.EnvironmentsParserResult.Success
import io.github.gerardorodriguezdev.chamaleon.core.utils.PrettyJson
import kotlinx.serialization.json.Json
import java.io.File

public interface EnvironmentsParser {
    public fun environmentsParserResult(environmentsDirectory: File): EnvironmentsParserResult
    public fun addEnvironments(
        environmentsDirectory: File,
        environments: Set<Environment>,
    ): Boolean
}

internal class DefaultEnvironmentsParser(
    val environmentFileMatcher: (File) -> Boolean,
    val environmentNameExtractor: (File) -> String,
    val environmentFileNameExtractor: (environmentName: String) -> String,
) : EnvironmentsParser {

    // TODO: Error if any env invalid
    override fun environmentsParserResult(environmentsDirectory: File): EnvironmentsParserResult {
        val environmentsDirectoryFiles = environmentsDirectory.listFiles()

        val environmentsFiles =
            environmentsDirectoryFiles?.filter { file -> environmentFileMatcher(file) } ?: emptyList()

        val environments = environmentsFiles.mapNotNull { file ->
            val environmentName = environmentNameExtractor(file)

            val fileContent = file.readText()
            if (fileContent.isEmpty()) return@mapNotNull null

            val platformDtos = try {
                Json.decodeFromString<Set<PlatformDto>>(fileContent)
            } catch (error: Exception) {
                return Failure.Serialization(error)
            }

            Environment(
                name = environmentName, // TODO: Validate not empty
                platforms = platformDtos.toPlatforms(),
            )
        }

        return Success(environments = environments.toSet())
    }

    @Suppress("ReturnCount")
    override fun addEnvironments(
        environmentsDirectory: File,
        environments: Set<Environment>,
    ): Boolean {
        if (environments.isEmpty()) return false // TODO: Error = no envs to add

        environments.forEach { environment ->
            // TODO: Don't allow empty env name. Error = env name is empty
            val environmentFileName = environmentFileNameExtractor(environment.name)
            val environmentFile = File(environmentsDirectory, environmentFileName)
            //TODO: Create files if not exist
            if (environmentFile.exists()) return false // TODO: Error = envs file already present

            try {
                val platformDtos = environment.platforms.toPlatformDtos() // TODO: Don't allow empty platfs
                val platformDtosJson = PrettyJson.encodeToString(platformDtos)
                environmentFile.writeText(platformDtosJson)
            } catch (_: Exception) {
                return false
            }
        }

        return true
    }

    private fun Set<PlatformDto>.toPlatforms(): Set<Platform> =
        map { platformDto ->
            Platform(
                platformType = platformDto.platformType, // TODO: Don't allow dup platforms
                properties = platformDto.properties.toProperties(), // TODO: Don't allow empty props
            )
        }.toSet()

    private fun Set<PlatformDto.PropertyDto>.toProperties(): Set<Platform.Property> =
        map { propertyDto ->
            Platform.Property(
                name = propertyDto.name, // TODO: Don't allow empty name + Don't allow dup name
                value = propertyDto.value, // TODO: Don't allow empty string if string value
            )
        }.toSet()

    private fun Set<Platform>.toPlatformDtos(): Set<PlatformDto> =
        map { platform -> platform.toPlatformDto() }.toSet()

    private fun Platform.toPlatformDto(): PlatformDto =
        PlatformDto(
            platformType = platformType, // TODO: Don't allow dup platforms
            properties = properties.toPropertyDtos() // TODO: Don't allow empty props list
        )

    private fun Set<Platform.Property>.toPropertyDtos(): Set<PlatformDto.PropertyDto> =
        mapNotNull { property -> property.toPropertyDto() }.toSet()

    private fun Platform.Property.toPropertyDto(): PlatformDto.PropertyDto? {
        return PlatformDto.PropertyDto(
            name = name, // TODO: Don't allow empty string + Don't allow dup names
            value = value ?: return null, // TODO: Don't allow empty string if string value type
        )
    }
}