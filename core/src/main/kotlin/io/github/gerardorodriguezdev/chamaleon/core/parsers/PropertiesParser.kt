package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser.PropertiesParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser.PropertiesParserResult.Failure.InvalidPropertiesFile
import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser.PropertiesParserResult.Failure.Parsing
import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser.PropertiesParserResult.Success
import java.io.File
import java.util.*

interface PropertiesParser {
    fun propertiesParserResult(propertiesFile: File): PropertiesParserResult

    sealed interface PropertiesParserResult {
        data class Success(val selectedEnvironmentName: String? = null) : PropertiesParserResult
        sealed interface Failure : PropertiesParserResult {
            data class Parsing(val throwable: Throwable) : Failure
            data class InvalidPropertiesFile(val path: String) : Failure
        }
    }
}

class DefaultPropertiesParser : PropertiesParser {
    @Suppress("ReturnCount")
    override fun propertiesParserResult(propertiesFile: File): PropertiesParserResult {
        if (!propertiesFile.exists()) return Success()

        return try {
            val properties = Properties()
            properties.load(propertiesFile.inputStream())

            if (properties.isEmpty) return Success()

            val selectedEnvironmentName = properties[SELECTED_ENVIRONMENT_KEY] as? String
            if (selectedEnvironmentName == null) return InvalidPropertiesFile(propertiesFile.parent)

            return Success(selectedEnvironmentName)
        } catch (error: Exception) {
            Parsing(error)
        }
    }

    private companion object {
        const val SELECTED_ENVIRONMENT_KEY = "CHAMALEON_SELECTED_ENVIRONMENT"
    }
}