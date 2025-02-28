package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.models.Properties
import io.github.gerardorodriguezdev.chamaleon.core.results.PropertiesParserResult
import io.github.gerardorodriguezdev.chamaleon.core.results.PropertiesParserResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.results.PropertiesParserResult.Success
import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.ValidFile
import kotlinx.serialization.json.Json

public interface PropertiesParser {
    public fun parse(propertiesFile: ValidFile): PropertiesParserResult
}

internal object DefaultPropertiesParser : PropertiesParser {

    override fun parse(propertiesFile: ValidFile): PropertiesParserResult {
        return try {
            val propertiesFile = propertiesFile.toExistingFile() ?: return Success(Properties())

            val propertiesFileContent = propertiesFile.file.readText()
            if (propertiesFileContent.isEmpty()) return Success(properties = Properties())

            val properties = Json.decodeFromString<Properties>(propertiesFileContent)

            return Success(properties = properties)
        } catch (error: Exception) {
            Failure.Serialization(propertiesFilePath = propertiesFile.path, throwable = error)
        }
    }
}