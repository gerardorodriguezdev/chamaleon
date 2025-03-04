package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.models.Schema
import io.github.gerardorodriguezdev.chamaleon.core.results.SchemaParserResult
import io.github.gerardorodriguezdev.chamaleon.core.results.SchemaParserResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.results.SchemaParserResult.Success
import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.ExistingFile
import kotlinx.serialization.json.Json

public interface SchemaParser {
    public fun parse(schemaFile: ExistingFile): SchemaParserResult
}

internal object DefaultSchemaParser : SchemaParser {

    override fun parse(schemaFile: ExistingFile): SchemaParserResult {
        return try {
            val schemaFileContent = schemaFile.readContent()
            if (schemaFileContent.isEmpty()) {
                return Failure.FileIsEmpty(
                    schemaFilePath = schemaFile.path.value
                )
            }

            val schema = Json.decodeFromString<Schema>(schemaFileContent)

            Success(schema)
        } catch (error: Exception) {
            Failure.Serialization(schemaFilePath = schemaFile.path.value, throwable = error)
        }
    }
}