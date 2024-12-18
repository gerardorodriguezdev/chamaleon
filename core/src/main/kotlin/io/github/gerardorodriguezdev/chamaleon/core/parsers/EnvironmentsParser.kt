package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.dtos.PlatformDto
import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform
import io.github.gerardorodriguezdev.chamaleon.core.parsers.EnvironmentsParser.EnvironmentsParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.EnvironmentsParser.EnvironmentsParserResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.parsers.EnvironmentsParser.EnvironmentsParserResult.Success
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File

interface EnvironmentsParser {
    fun environmentsParserResult(directory: File): EnvironmentsParserResult

    sealed interface EnvironmentsParserResult {
        data class Success(val environments: Set<Environment>) : EnvironmentsParserResult

        sealed interface Failure : EnvironmentsParserResult {
            data class Serialization(val throwable: Throwable) : Failure
        }
    }
}

internal class DefaultEnvironmentsParser : EnvironmentsParser {
    override fun environmentsParserResult(directory: File): EnvironmentsParserResult {
        val directoryFiles = directory.listFiles()

        val jsonFiles = directoryFiles?.filter { file -> file.isEnvironmentFile } ?: emptyList()

        val environments = jsonFiles.mapNotNull { file ->
            val environmentName = file.name.removeSuffix(ENVIRONMENT_FILE_SUFFIX)

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

    private val File.isEnvironmentFile: Boolean get() = name.endsWith(ENVIRONMENT_FILE_SUFFIX)

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

    private companion object {
        const val ENVIRONMENT_FILE_SUFFIX = "-cha.json"
    }
}