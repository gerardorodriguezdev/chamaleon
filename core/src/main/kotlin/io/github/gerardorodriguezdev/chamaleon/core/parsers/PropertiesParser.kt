package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.dtos.PropertiesDto
import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser.PropertiesParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser.PropertiesParserResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser.PropertiesParserResult.Success
import io.github.gerardorodriguezdev.chamaleon.core.utils.PrettyJson
import kotlinx.serialization.json.Json
import java.io.File

public interface PropertiesParser {
    public fun propertiesParserResult(propertiesFile: File): PropertiesParserResult
    public fun addOrUpdateSelectedEnvironment(propertiesFile: File, newSelectedEnvironment: String?): Boolean

    //TODO: Move out
    public sealed interface PropertiesParserResult {
        public data class Success(val selectedEnvironmentName: String? = null) : PropertiesParserResult
        public sealed interface Failure : PropertiesParserResult {
            public data object InvalidPropertiesFile : Failure
            public data class Serialization(val throwable: Exception) : Failure
        }
    }
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