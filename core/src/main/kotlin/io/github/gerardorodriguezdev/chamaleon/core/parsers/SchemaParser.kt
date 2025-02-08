package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.dtos.SchemaDto
import io.github.gerardorodriguezdev.chamaleon.core.dtos.SchemaDto.PropertyDefinitionDto
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema.PropertyDefinition
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.AddSchemaResult
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.SchemaParserResult
import io.github.gerardorodriguezdev.chamaleon.core.utils.PrettyJson
import kotlinx.serialization.SerializationException
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
    @Suppress("ReturnCount")
    override fun schemaParserResult(schemaFile: File): SchemaParserResult {
        if (!schemaFile.exists()) return SchemaParserResult.Failure.FileNotFound(schemaFile.path)

        val schemaFileContent = schemaFile.readText()
        if (schemaFileContent.isEmpty()) return SchemaParserResult.Failure.FileIsEmpty(schemaFile.path)

        return try {
            val schemaDto = Json.decodeFromString<SchemaDto>(schemaFileContent)

            val verificationResult = schemaDto.isValid().toFailureOrNull(path = schemaFile.path)
            if (verificationResult != null) return verificationResult

            SchemaParserResult.Success(schemaDto.toSchema())
        } catch (exception: SerializationException) {
            SchemaParserResult.Failure.Serialization(exception)
        }
    }

    //TODO: Test
    //TODO: Update processor
    override fun addSchema(
        schemaFile: File,
        newSchema: Schema
    ): AddSchemaResult {
        //TODO: Create file if not present. If present return error. Remote file not found error
        if (!schemaFile.exists()) return AddSchemaResult.Failure.FileNotFound(schemaFile.path)
        if (schemaFile.isDirectory) return AddSchemaResult.Failure.InvalidFile(schemaFile.path)

        val verificationResult = newSchema.isValid().toFailureOrNull(schemaFile.path)
        if (verificationResult != null) return verificationResult

        val schemaDto = newSchema.toSchemaDto()
        return try {
            val schemaFileContent = PrettyJson.encodeToString(schemaDto)
            schemaFile.writeText(schemaFileContent)

            AddSchemaResult.Success
        } catch (error: Exception) {
            AddSchemaResult.Failure.Serialization(schemaFile.path, error)
        }
    }

    private fun SchemaDto.ValidationResult.toFailureOrNull(path: String): SchemaParserResult.Failure? =
        when (this) {
            SchemaDto.ValidationResult.VALID -> null
            SchemaDto.ValidationResult.EMPTY_SUPPORTED_PLATFORMS -> SchemaParserResult.Failure.EmptySupportedPlatforms(
                path
            )

            SchemaDto.ValidationResult.EMPTY_PROPERTY_DEFINITIONS -> SchemaParserResult.Failure.EmptyPropertyDefinitions(
                path
            )

            SchemaDto.ValidationResult.INVALID_PROPERTY_DEFINITION -> SchemaParserResult.Failure.InvalidPropertyDefinition(
                path
            )

            SchemaDto.ValidationResult.DUPLICATED_PROPERTY_DEFINITION -> SchemaParserResult.Failure.DuplicatedPropertyDefinition(
                path
            )
        }

    private fun Schema.ValidationResult.toFailureOrNull(path: String): AddSchemaResult.Failure? =
        when (this) {
            Schema.ValidationResult.VALID -> null
            Schema.ValidationResult.EMPTY_SUPPORTED_PLATFORMS -> AddSchemaResult.Failure.EmptySupportedPlatforms(path)
            Schema.ValidationResult.EMPTY_PROPERTY_DEFINITIONS -> AddSchemaResult.Failure.EmptyPropertyDefinitions(path)
            Schema.ValidationResult.INVALID_PROPERTY_DEFINITION -> AddSchemaResult.Failure.InvalidPropertyDefinition(
                path
            )

            Schema.ValidationResult.DUPLICATED_PROPERTY_DEFINITION -> AddSchemaResult.Failure.DuplicatedPropertyDefinition(
                path
            )
        }

    private fun SchemaDto.toSchema(): Schema =
        Schema(
            supportedPlatforms = this@toSchema.supportedPlatforms,
            propertyDefinitions = propertyDefinitionDtos.toPropertyDefinitions(),
        )

    private fun Schema.toSchemaDto(): SchemaDto =
        SchemaDto(
            supportedPlatforms = this@toSchemaDto.supportedPlatforms,
            propertyDefinitionDtos = propertyDefinitions.toPropertyDefinitionsDtos(),
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