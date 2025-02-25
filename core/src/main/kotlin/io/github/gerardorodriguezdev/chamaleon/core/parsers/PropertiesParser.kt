package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.dtos.PropertiesDto
import io.github.gerardorodriguezdev.chamaleon.core.results.PropertiesParserResult
import io.github.gerardorodriguezdev.chamaleon.core.results.PropertiesParserResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.results.PropertiesParserResult.Success
import kotlinx.serialization.json.Json
import java.io.File

public interface PropertiesParser {
    public fun propertiesParserResult(propertiesFile: File): PropertiesParserResult
}

internal object DefaultPropertiesParser : PropertiesParser {

    override fun propertiesParserResult(propertiesFile: File): PropertiesParserResult {
        return try {
            if (!propertiesFile.isFile) return Failure.InvalidFile(propertiesFile.path)
            if (!propertiesFile.exists()) return Success()

            val propertiesFileContent = propertiesFile.readText()
            if (propertiesFileContent.isEmpty()) return Success()

            val propertiesDto = Json.decodeFromString<PropertiesDto>(propertiesFileContent)

            return Success(selectedEnvironmentName = propertiesDto.selectedEnvironmentName)
        } catch (error: Exception) {
            Failure.Serialization(propertiesFilePath = propertiesFile.path, throwable = error)
        }
    }
}