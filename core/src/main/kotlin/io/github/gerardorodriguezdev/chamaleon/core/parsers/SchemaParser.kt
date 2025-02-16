package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.dtos.SchemaDto
import io.github.gerardorodriguezdev.chamaleon.core.dtos.SchemaDto.PropertyDefinitionDto
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema.PropertyDefinition
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema.ValidationResult
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.AddSchemaResult
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.SchemaParserResult
import io.github.gerardorodriguezdev.chamaleon.core.utils.PrettyJson
import kotlinx.serialization.json.Json
import java.io.File

public interface SchemaParser {
    public fun schemaParserResult(schemaFile: File): SchemaParserResult
    public fun addSchema(
        schemaFile: File,
        schema: Schema
    ): AddSchemaResult
}

internal class DefaultSchemaParser : SchemaParser {
    @Suppress("ReturnCount", "TooGenericExceptionCaught")
    override fun schemaParserResult(schemaFile: File): SchemaParserResult {
        return try {
            if (!schemaFile.exists()) return SchemaParserResult.Failure.FileNotFound(schemaFile.path)
            val schemaFileContent = schemaFile.readText()
            if (schemaFileContent.isEmpty()) return SchemaParserResult.Failure.FileIsEmpty(schemaFile.path)

            val schemaDto = Json.decodeFromString<SchemaDto>(schemaFileContent)
            SchemaParserResult.Success(schemaDto.toSchema())
        } catch (error: Exception) {
            SchemaParserResult.Failure.Serialization(error)
        }
    }

    @Suppress("ReturnCount", "TooGenericExceptionCaught")
    override fun addSchema(
        schemaFile: File,
        newSchema: Schema
    ): AddSchemaResult {
        return try {
            if (schemaFile.exists()) return AddSchemaResult.Failure.FileAlreadyPresent(schemaFile.path)
            schemaFile.createNewFile()

            if (schemaFile.isDirectory) return AddSchemaResult.Failure.InvalidFile(schemaFile.path)

            val verificationResult = newSchema.isValid().toFailureOrNull(schemaFile.path)
            if (verificationResult != null) return verificationResult

            val schemaDto = newSchema.toSchemaDto()

            val schemaFileContent = PrettyJson.encodeToString(schemaDto)
            schemaFile.writeText(schemaFileContent)

            AddSchemaResult.Success
        } catch (error: Exception) {
            AddSchemaResult.Failure.Serialization(error)
        }
    }

    private fun ValidationResult.toFailureOrNull(path: String): AddSchemaResult.Failure? =
        when (this) {
            ValidationResult.VALID -> null
            ValidationResult.EMPTY_SUPPORTED_PLATFORMS -> AddSchemaResult.Failure.EmptySupportedPlatforms(path)
            ValidationResult.EMPTY_PROPERTY_DEFINITIONS -> AddSchemaResult.Failure.EmptyPropertyDefinitions(path)
            ValidationResult.INVALID_PROPERTY_DEFINITION -> AddSchemaResult.Failure.InvalidPropertyDefinition(path)
            ValidationResult.DUPLICATED_PROPERTY_DEFINITION -> AddSchemaResult.Failure.DuplicatedPropertyDefinition(
                path
            )
        }

    private fun SchemaDto.toSchema(): Schema =
        Schema(
            supportedPlatforms = this@toSchema.supportedPlatforms,
            propertyDefinitions = propertyDefinitionsDtos.toPropertyDefinitions(),
        )

    private fun Set<PropertyDefinitionDto>.toPropertyDefinitions(): Set<PropertyDefinition> =
        map { propertyDefinitionDto ->
            PropertyDefinition(
                name = propertyDefinitionDto.name,
                propertyType = propertyDefinitionDto.propertyType,
                nullable = propertyDefinitionDto.nullable,
                supportedPlatforms = propertyDefinitionDto.supportedPlatforms,
            )
        }.toSet()

    private fun Schema.toSchemaDto(): SchemaDto =
        SchemaDto(
            supportedPlatforms = this@toSchemaDto.supportedPlatforms,
            propertyDefinitionsDtos = propertyDefinitions.toPropertyDefinitionsDtos(),
        )

    private fun Set<PropertyDefinition>.toPropertyDefinitionsDtos(): Set<PropertyDefinitionDto> =
        map { propertyDefinition ->
            PropertyDefinitionDto(
                name = propertyDefinition.name,
                propertyType = propertyDefinition.propertyType,
                nullable = propertyDefinition.nullable,
                supportedPlatforms = propertyDefinition.supportedPlatforms,
            )
        }.toSet()
}