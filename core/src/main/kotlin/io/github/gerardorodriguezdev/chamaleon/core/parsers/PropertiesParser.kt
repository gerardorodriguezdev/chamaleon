package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.dtos.PropertiesDto
import io.github.gerardorodriguezdev.chamaleon.core.results.AddOrUpdateSelectedEnvironmentResult
import io.github.gerardorodriguezdev.chamaleon.core.results.PropertiesParserResult
import io.github.gerardorodriguezdev.chamaleon.core.utils.PrettyJson
import kotlinx.serialization.json.Json
import java.io.File

public interface PropertiesParser {
    public fun propertiesParserResult(propertiesFile: File): PropertiesParserResult
    public fun addOrUpdateSelectedEnvironment(
        propertiesFile: File,
        newSelectedEnvironment: String?
    ): AddOrUpdateSelectedEnvironmentResult
}

internal class DefaultPropertiesParser : PropertiesParser {

    override fun propertiesParserResult(propertiesFile: File): PropertiesParserResult {
        return try {
            if (!propertiesFile.isFile) return PropertiesParserResult.Failure.InvalidFile(propertiesFile.path)
            if (!propertiesFile.exists()) return PropertiesParserResult.Success()

            val propertiesFileContent = propertiesFile.readText()
            if (propertiesFileContent.isEmpty()) return PropertiesParserResult.Success()

            val propertiesDto = Json.decodeFromString<PropertiesDto>(propertiesFileContent)

            return PropertiesParserResult.Success(selectedEnvironmentName = propertiesDto.selectedEnvironmentName)
        } catch (error: Exception) {
            PropertiesParserResult.Failure.Serialization(propertiesFilePath = propertiesFile.path, throwable = error)
        }
    }

    override fun addOrUpdateSelectedEnvironment(
        propertiesFile: File,
        newSelectedEnvironment: String?,
    ): AddOrUpdateSelectedEnvironmentResult {
        return try {
            if (!propertiesFile.isFile) {
                return AddOrUpdateSelectedEnvironmentResult.Failure.InvalidFile(
                    propertiesFilePath = propertiesFile.path,
                )
            }
            if (newSelectedEnvironment != null && newSelectedEnvironment.isEmpty()) {
                return AddOrUpdateSelectedEnvironmentResult.Failure.EnvironmentNameIsEmpty(
                    propertiesFilePath = propertiesFile.path,
                )
            }

            if (!propertiesFile.exists()) propertiesFile.createNewFile()

            val propertiesDto = PropertiesDto(newSelectedEnvironment)
            val propertiesFileContent = PrettyJson.encodeToString(propertiesDto)
            propertiesFile.writeText(propertiesFileContent)

            return AddOrUpdateSelectedEnvironmentResult.Success
        } catch (error: Exception) {
            return AddOrUpdateSelectedEnvironmentResult.Failure.Serialization(
                propertiesFilePath = propertiesFile.path,
                throwable = error
            )
        }
    }
}