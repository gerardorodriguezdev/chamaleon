package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.models.Properties
import io.github.gerardorodriguezdev.chamaleon.core.results.PropertiesParserResult
import io.github.gerardorodriguezdev.chamaleon.core.results.PropertiesParserResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.results.PropertiesParserResult.Success
import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.ExistingFile
import kotlinx.serialization.json.Json

public interface PropertiesParser {
    public fun parse(propertiesFile: ExistingFile): PropertiesParserResult
}

internal object DefaultPropertiesParser : PropertiesParser {

    override fun parse(propertiesFile: ExistingFile): PropertiesParserResult {
        return try {
            val propertiesFileContent = propertiesFile.readContent()
            if (propertiesFileContent.isEmpty()) return Success()

            val properties = Json.decodeFromString<Properties>(propertiesFileContent)

            return Success(properties = properties)
        } catch (error: Exception) {
            Failure.Serialization(propertiesFilePath = propertiesFile.path.value, throwable = error)
        }
    }
}