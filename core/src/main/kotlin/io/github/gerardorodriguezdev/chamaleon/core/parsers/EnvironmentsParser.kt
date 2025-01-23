package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.dtos.PlatformDto
import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Platform
import io.github.gerardorodriguezdev.chamaleon.core.parsers.EnvironmentsParser.EnvironmentsParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.EnvironmentsParser.EnvironmentsParserResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.parsers.EnvironmentsParser.EnvironmentsParserResult.Success
import io.github.gerardorodriguezdev.chamaleon.core.utils.PrettyJson
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File

public interface EnvironmentsParser {
    public fun environmentsParserResult(environmentsDirectory: File): EnvironmentsParserResult
    public fun addEnvironments(
        environmentsDirectory: File,
        environments: Set<Environment>,
    ): Boolean

    public sealed interface EnvironmentsParserResult {
        public data class Success(val environments: Set<Environment>) : EnvironmentsParserResult

        public sealed interface Failure : EnvironmentsParserResult {
            public data class Serialization(val throwable: Throwable) : Failure
        }
    }
}

internal class DefaultEnvironmentsParser(
    val environmentFileMatcher: (File) -> Boolean,
    val environmentNameExtractor: (File) -> String,
    val environmentFileNameExtractor: (environmentName: String) -> String,
) : EnvironmentsParser {

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
            } catch (error: SerializationException) {
                return Failure.Serialization(error)
            }

            Environment(
                name = environmentName,
                platforms = platformDtos.toPlatforms(),
            )
        }

        return Success(environments = environments.toSet())
    }

    override fun addEnvironments(
        environmentsDirectory: File,
        environments: Set<Environment>,
    ): Boolean {
        if (environments.isEmpty()) return false

        environments.forEach { environment ->
            val environmentFileName = environmentFileNameExtractor(environment.name)
            val environmentFile = File(environmentsDirectory, environmentFileName)
            if (environmentFile.exists()) return false

            try {
                val platformDtos = environment.platforms.toPlatformDtos()
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