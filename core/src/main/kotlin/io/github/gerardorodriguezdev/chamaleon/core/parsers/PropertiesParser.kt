package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.dtos.PropertiesDto
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.PropertiesParserResult
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.PropertiesParserResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.PropertiesParserResult.Success
import io.github.gerardorodriguezdev.chamaleon.core.utils.PrettyJson
import kotlinx.serialization.json.Json
import java.io.File

public interface PropertiesParser {
    public fun propertiesParserResult(propertiesFile: File): PropertiesParserResult
    public fun addOrUpdateSelectedEnvironment(propertiesFile: File, newSelectedEnvironment: String?): Boolean
}

internal class DefaultPropertiesParser : PropertiesParser {
    @Suppress("ReturnCount")
    override fun propertiesParserResult(propertiesFile: File): PropertiesParserResult {
        if (!propertiesFile.exists()) return Success()

        return try {
            val propertiesFileContent = propertiesFile.readText()
            if (propertiesFileContent.isEmpty()) return Success()

            val propertiesDto = Json.decodeFromString<PropertiesDto>(propertiesFileContent)

            return Success(selectedEnvironmentName = propertiesDto.selectedEnvironmentName)
        } catch (error: Exception) {
            Failure.Serialization(error)
        }
    }

    @Suppress("ReturnCount")
    override fun addOrUpdateSelectedEnvironment(propertiesFile: File, newSelectedEnvironment: String?): Boolean {
        try {
            if (propertiesFile.isDirectory) return false
            if (!propertiesFile.exists()) propertiesFile.createNewFile()
            if (newSelectedEnvironment != null && newSelectedEnvironment.isEmpty()) return false
            val propertiesDto =
                PropertiesDto(
                    selectedEnvironmentName = newSelectedEnvironment
                )
            val propertiesFileContent = PrettyJson.encodeToString(propertiesDto)
            propertiesFile.writeText(propertiesFileContent)
            return true
        } catch (_: Exception) {
            return false
        }
    }
}