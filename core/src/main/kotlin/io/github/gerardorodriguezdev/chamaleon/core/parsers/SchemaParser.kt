package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.dtos.SchemaDto
import io.github.gerardorodriguezdev.chamaleon.core.dtos.SchemaDto.PropertyDefinitionDto
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema.PropertyDefinition
import io.github.gerardorodriguezdev.chamaleon.core.parsers.SchemaParser.SchemaParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.SchemaParser.SchemaParserResult.Failure
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File

interface SchemaParser {
    fun schemaParserResult(): SchemaParserResult

    sealed interface SchemaParserResult {
        data class Success(val schema: Schema) : SchemaParserResult

        sealed interface Failure : SchemaParserResult {
            data class FileNotFound(val path: String) : Failure
            data class FileIsEmpty(val path: String) : Failure
            data class Serialization(val throwable: Throwable) : Failure
        }
    }
}

internal class DefaultSchemaParser(
    val directory: File,
    val schemaFileName: String,
) : SchemaParser {

    @Suppress("ReturnCount")
    override fun schemaParserResult(): SchemaParserResult {
        val schemaFile = File(directory, schemaFileName)
        if (!schemaFile.exists()) return Failure.FileNotFound(directory.path)

        val schemaFileContent = schemaFile.readText()
        if (schemaFileContent.isEmpty()) return Failure.FileIsEmpty(directory.path)

        return try {
            val schemaDto = Json.decodeFromString<SchemaDto>(schemaFileContent)
            SchemaParserResult.Success(schemaDto.toSchema())
        } catch (exception: SerializationException) {
            Failure.Serialization(exception)
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