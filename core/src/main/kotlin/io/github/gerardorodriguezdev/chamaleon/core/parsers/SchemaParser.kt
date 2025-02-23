package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.dtos.SchemaDto
import io.github.gerardorodriguezdev.chamaleon.core.dtos.SchemaDto.PropertyDefinitionDto
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema.PropertyDefinition
import io.github.gerardorodriguezdev.chamaleon.core.results.AddSchemaResult
import io.github.gerardorodriguezdev.chamaleon.core.results.SchemaParserResult
import io.github.gerardorodriguezdev.chamaleon.core.utils.PrettyJson
import kotlinx.serialization.json.Json
import java.io.File

public interface SchemaParser {
    public fun schemaParserResult(schemaFile: File): SchemaParserResult
    public fun addSchema(
        schemaFile: File,
        newSchema: Schema
    ): AddSchemaResult
}

internal class DefaultSchemaParser : SchemaParser {

    override fun schemaParserResult(schemaFile: File): SchemaParserResult {
        return try {
            if (!schemaFile.exists()) return SchemaParserResult.Failure.FileNotFound(schemaFilePath = schemaFile.path)

            val schemaFileContent = schemaFile.readText()
            if (schemaFileContent.isEmpty()) return SchemaParserResult.Failure.FileIsEmpty(schemaFilePath = schemaFile.path)

            val schemaDto = Json.decodeFromString<SchemaDto>(schemaFileContent)

            SchemaParserResult.Success(schemaDto.toSchema())
        } catch (error: Exception) {
            SchemaParserResult.Failure.Serialization(schemaFilePath = schemaFile.path, throwable = error)
        }
    }

    private fun SchemaDto.toSchema(): Schema =
        Schema(
            globalSupportedPlatformTypes,
            propertyDefinitionsDtos.toPropertyDefinitions(),
        )

    private fun Set<PropertyDefinitionDto>.toPropertyDefinitions(): Set<PropertyDefinition> =
        map { propertyDefinitionDto ->
            PropertyDefinition(
                name = propertyDefinitionDto.name,
                propertyType = propertyDefinitionDto.propertyType,
                nullable = propertyDefinitionDto.nullable,
                supportedPlatformTypes = propertyDefinitionDto.supportedPlatformTypes,
            )
        }.toSet()

    override fun addSchema(
        schemaFile: File,
        newSchema: Schema
    ): AddSchemaResult {
        return try {
            if (schemaFile.isDirectory) return AddSchemaResult.Failure.InvalidFile(schemaFilePath = schemaFile.path)

            if (schemaFile.exists()) schemaFile.createNewFile()

            val schemaDto = newSchema.toSchemaDto()
            val schemaFileContent = PrettyJson.encodeToString(schemaDto)
            schemaFile.writeText(schemaFileContent)

            AddSchemaResult.Success
        } catch (error: Exception) {
            AddSchemaResult.Failure.Serialization(schemaFilePath = schemaFile.path, throwable = error)
        }
    }

    private fun Schema.toSchemaDto(): SchemaDto =
        SchemaDto(
            globalSupportedPlatformTypes,
            propertyDefinitions.toPropertyDefinitionsDtos(),
        )

    private fun Set<PropertyDefinition>.toPropertyDefinitionsDtos(): Set<PropertyDefinitionDto> =
        map { propertyDefinition ->
            PropertyDefinitionDto(
                name = propertyDefinition.name,
                propertyType = propertyDefinition.propertyType,
                nullable = propertyDefinition.nullable,
                supportedPlatformTypes = propertyDefinition.supportedPlatformTypes,
            )
        }.toSet()
}