package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.dtos.PropertiesDto
import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser.PropertiesParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser.PropertiesParserResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser.PropertiesParserResult.Success
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

public interface PropertiesParser {
    public fun propertiesParserResult(propertiesFile: File): PropertiesParserResult
    public fun updateSelectedEnvironment(propertiesFile: File, newSelectedEnvironment: String?): Boolean

    public sealed interface PropertiesParserResult {
        public data class Success(val selectedEnvironmentName: String? = null) : PropertiesParserResult
        public data class Failure(val throwable: Throwable) : PropertiesParserResult
    }
}

internal class DefaultPropertiesParser : PropertiesParser {
    @Suppress("ReturnCount")
    override fun propertiesParserResult(propertiesFile: File): PropertiesParserResult {
        if (!propertiesFile.exists()) return Success()

        val propertiesFileContent = propertiesFile.readText()
        if (propertiesFileContent.isEmpty()) return Success()

        return try {
            val propertiesDto = Json.decodeFromString<PropertiesDto>(propertiesFileContent)
            return Success(selectedEnvironmentName = propertiesDto.selectedEnvironmentName)
        } catch (error: IllegalArgumentException) {
            Failure(error)
        } catch (error: SerializationException) {
            Failure(error)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Suppress("ReturnCount")
    override fun updateSelectedEnvironment(propertiesFile: File, newSelectedEnvironment: String?): Boolean {
        try {
            val propertiesDto = PropertiesDto(selectedEnvironmentName = newSelectedEnvironment)
            val propertiesFileContent = prettyJson.encodeToString(propertiesDto)
            propertiesFile.writeText(propertiesFileContent)
            return true
        } catch (_: Exception) {
            return false
        }
    }

    private companion object {
        const val INDENTATION = "  "

        @OptIn(ExperimentalSerializationApi::class)
        val prettyJson = Json {
            prettyPrint = true
            prettyPrintIndent = INDENTATION
        }
    }
}