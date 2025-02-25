package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.dtos.SchemaDto
import io.github.gerardorodriguezdev.chamaleon.core.mappers.SchemaMapperImpl
import io.github.gerardorodriguezdev.chamaleon.core.results.SchemaParserResult
import io.github.gerardorodriguezdev.chamaleon.core.results.SchemaParserResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.results.SchemaParserResult.Success
import kotlinx.serialization.json.Json
import java.io.File

public interface SchemaParser {
    public fun parse(schemaFile: File): SchemaParserResult
}

internal object DefaultSchemaParser : SchemaParser {

    override fun parse(schemaFile: File): SchemaParserResult {
        return try {
            if (!schemaFile.isFile) return Failure.InvalidFile(schemaFilePath = schemaFile.path)
            if (!schemaFile.exists()) return Failure.FileNotFound(schemaFilePath = schemaFile.path)

            val schemaFileContent = schemaFile.readText()
            if (schemaFileContent.isEmpty()) return Failure.FileIsEmpty(
                schemaFilePath = schemaFile.path
            )

            val schemaDto = Json.decodeFromString<SchemaDto>(schemaFileContent)

            Success(SchemaMapperImpl.toModel(schemaDto))
        } catch (error: Exception) {
            Failure.Serialization(schemaFilePath = schemaFile.path, throwable = error)
        }
    }
}