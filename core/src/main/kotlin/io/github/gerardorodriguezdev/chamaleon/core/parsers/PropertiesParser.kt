package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.dtos.PropertiesDto
import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser.PropertiesParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser.PropertiesParserResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser.PropertiesParserResult.Success
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

interface PropertiesParser {
    fun propertiesParserResult(propertiesFile: File): PropertiesParserResult
    fun updateSelectedEnvironment(propertiesFile: File, newSelectedEnvironment: String?): Boolean

    sealed interface PropertiesParserResult {
        data class Success(val selectedEnvironmentName: String? = null) : PropertiesParserResult
        data class Failure(val throwable: Throwable) : PropertiesParserResult
    }
}

class DefaultPropertiesParser : PropertiesParser {
    @Suppress("ReturnCount")
    override fun propertiesParserResult(propertiesFile: File): PropertiesParserResult {
        if (!propertiesFile.exists()) return Success()

        val propertiesFileContent = propertiesFile.readText()
        if (propertiesFileContent.isEmpty()) return Success()

        return try {
            val propertiesDto = Json.decodeFromString<PropertiesDto>(propertiesFileContent)
            return Success(selectedEnvironmentName = propertiesDto.selectedEnvironmentName)
        } catch (error: Exception) {
            Failure(error)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun updateSelectedEnvironment(propertiesFile: File, newSelectedEnvironment: String?): Boolean {
        try {
            if (newSelectedEnvironment == null) {
                propertiesFile.writeText("")
                return true
            } else {
                val propertiesDto = PropertiesDto(selectedEnvironmentName = newSelectedEnvironment)
                val prettyJson = Json {
                    prettyPrint = true
                    prettyPrintIndent = "  "
                }
                val propertiesFileContent = prettyJson.encodeToString(propertiesDto)
                propertiesFile.writeText(propertiesFileContent)
                return true
            }
        } catch (_: Exception) {
            return false
        }
    }
}