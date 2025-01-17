package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.dtos.PlatformDto
import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Platform
import io.github.gerardorodriguezdev.chamaleon.core.parsers.EnvironmentsParser.EnvironmentsParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.EnvironmentsParser.EnvironmentsParserResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.parsers.EnvironmentsParser.EnvironmentsParserResult.Success
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File

public interface EnvironmentsParser {
    public fun environmentsParserResult(environmentsDirectory: File): EnvironmentsParserResult

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
}