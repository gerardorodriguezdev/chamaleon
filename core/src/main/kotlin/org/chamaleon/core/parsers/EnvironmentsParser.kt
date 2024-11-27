package org.chamaleon.core.parsers

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.chamaleon.core.dtos.PlatformDto
import org.chamaleon.core.models.Environment
import org.chamaleon.core.models.Platform
import org.chamaleon.core.parsers.EnvironmentsParser.EnvironmentsParserResult
import org.chamaleon.core.parsers.EnvironmentsParser.EnvironmentsParserResult.Failure
import org.chamaleon.core.parsers.EnvironmentsParser.EnvironmentsParserResult.Success
import org.chamaleon.core.parsers.SchemaParser.Companion.SCHEMA_FILE
import java.io.File

interface EnvironmentsParser {
    fun environmentsParserResult(): EnvironmentsParserResult

    sealed interface EnvironmentsParserResult {
        data class Success(val environments: Set<Environment>) : EnvironmentsParserResult

        sealed interface Failure : EnvironmentsParserResult {
            data class SerializationError(val throwable: Throwable) : Failure
        }
    }
}

internal class DefaultEnvironmentsParser(private val directory: File) : EnvironmentsParser {
    override fun environmentsParserResult(): EnvironmentsParserResult {
        val directoryFiles = directory.listFiles()

        val jsonFiles = directoryFiles?.filter { file ->
            file.name != SCHEMA_FILE && file.extension.endsWith("json")
        } ?: emptyList()

        val environments = jsonFiles.mapNotNull { file ->
            val environmentName = file.nameWithoutExtension

            val fileContent = file.readText()
            if (fileContent.isEmpty()) return@mapNotNull null

            val platformDtos = try {
                Json.decodeFromString<Set<PlatformDto>>(fileContent)
            } catch (error: SerializationException) {
                return Failure.SerializationError(error)
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
