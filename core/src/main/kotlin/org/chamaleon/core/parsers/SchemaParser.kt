package org.chamaleon.core.parsers

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.chamaleon.core.dtos.SchemaDto
import org.chamaleon.core.dtos.SchemaDto.PropertyDefinitionDto
import org.chamaleon.core.models.Schema
import org.chamaleon.core.models.Schema.PropertyDefinition
import org.chamaleon.core.parsers.SchemaParser.SchemaParserResult
import org.chamaleon.core.parsers.SchemaParser.SchemaParserResult.Failure
import java.io.File

interface SchemaParser {
    fun schemaParserResult(): SchemaParserResult

    sealed interface SchemaParserResult {
        data class Success(val schema: Schema) : SchemaParserResult

        sealed interface Failure : SchemaParserResult {
            data class FileNotFound(val path: String) : Failure
            data class FileIsEmpty(val path: String) : Failure
            data class SerializationError(val throwable: Throwable) : Failure
        }
    }
}

internal class DefaultSchemaParser(
    val directory: File,
    val schemaFileName: String,
) : SchemaParser {
    override fun schemaParserResult(): SchemaParserResult {
        val schemaFile = File(directory, schemaFileName)
        if (!schemaFile.exists()) return Failure.FileNotFound(directory.path)

        val schemaFileContent = schemaFile.readText()
        if (schemaFileContent.isEmpty()) return Failure.FileIsEmpty(directory.path)

        return try {
            val schemaDto = Json.decodeFromString<SchemaDto>(schemaFileContent)
            SchemaParserResult.Success(schemaDto.toSchema())
        } catch (exception: SerializationException) {
            Failure.SerializationError(exception)
        }
    }

    private fun SchemaDto.toSchema(): Schema =
        Schema(
            supportedPlatforms = supportedPlatforms,
            propertyDefinitions = propertyDefinitionDtos.toPropertyDefinitions(),
        )

    private fun Set<PropertyDefinitionDto>.toPropertyDefinitions(): Set<PropertyDefinition> =
        map { propertyDefinitionDto ->
            PropertyDefinition(
                name = propertyDefinitionDto.name,
                propertyType = propertyDefinitionDto.propertyType,
                nullable = propertyDefinitionDto.nullable,
            )
        }.toSet()
}
