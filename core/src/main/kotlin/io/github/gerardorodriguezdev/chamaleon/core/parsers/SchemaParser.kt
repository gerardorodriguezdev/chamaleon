package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.dtos.SchemaDto
import io.github.gerardorodriguezdev.chamaleon.core.mappers.SchemaMapperImpl
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema
import io.github.gerardorodriguezdev.chamaleon.core.results.AddSchemaResult
import io.github.gerardorodriguezdev.chamaleon.core.results.SchemaParserResult
import io.github.gerardorodriguezdev.chamaleon.core.utils.PrettyJson
import kotlinx.serialization.json.Json
import java.io.File

public interface SchemaParser {
    public fun schemaParserResult(schemaFile: File): SchemaParserResult
    public fun addSchema(schemaFile: File, newSchema: Schema): AddSchemaResult
}

internal class DefaultSchemaParser : SchemaParser {

    override fun schemaParserResult(schemaFile: File): SchemaParserResult {
        return try {
            if (!schemaFile.isFile) return SchemaParserResult.Failure.InvalidFile(schemaFilePath = schemaFile.path)
            if (!schemaFile.exists()) return SchemaParserResult.Failure.FileNotFound(schemaFilePath = schemaFile.path)

            val schemaFileContent = schemaFile.readText()
            if (schemaFileContent.isEmpty()) return SchemaParserResult.Failure.FileIsEmpty(
                schemaFilePath = schemaFile.path
            )

            val schemaDto = Json.decodeFromString<SchemaDto>(schemaFileContent)

            SchemaParserResult.Success(SchemaMapperImpl.toModel(schemaDto))
        } catch (error: Exception) {
            SchemaParserResult.Failure.Serialization(schemaFilePath = schemaFile.path, throwable = error)
        }
    }

    override fun addSchema(schemaFile: File, newSchema: Schema): AddSchemaResult {
        return try {
            if (!schemaFile.isFile) return AddSchemaResult.Failure.InvalidFile(schemaFilePath = schemaFile.path)
            if (!schemaFile.exists()) return AddSchemaResult.Failure.FileAlreadyPresent(
                schemaFilePath = schemaFile.path
            )

            schemaFile.createNewFile()

            val schemaDto = SchemaMapperImpl.toDto(newSchema)
            val schemaFileContent = PrettyJson.encodeToString(schemaDto)
            schemaFile.writeText(schemaFileContent)

            AddSchemaResult.Success
        } catch (error: Exception) {
            AddSchemaResult.Failure.Serialization(schemaFilePath = schemaFile.path, throwable = error)
        }
    }
}