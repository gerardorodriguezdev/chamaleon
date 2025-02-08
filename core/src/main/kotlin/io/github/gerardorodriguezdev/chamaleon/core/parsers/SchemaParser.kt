package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.dtos.SchemaDto
import io.github.gerardorodriguezdev.chamaleon.core.dtos.SchemaDto.PropertyDefinitionDto
import io.github.gerardorodriguezdev.chamaleon.core.dtos.SchemaDto.ValidationResult.*
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema.PropertyDefinition
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.SchemaParserResult
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.SchemaParserResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.parsers.SchemaParser.AddSchemaResult
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

    public sealed interface AddSchemaResult {
        public data object Success : AddSchemaResult
        public sealed interface Failure : AddSchemaResult {
            public data class EmptySupportedPlatforms(val path: String) : Failure
            public data class EmptyPropertyDefinitions(val path: String) : Failure
            public data class InvalidPropertyDefinition(val path: String) : Failure
            public data class DuplicatedPropertyDefinition(val path: String) : Failure
            public data class FileNotFound(val path: String) : Failure
            public data class InvalidFile(val path: String) : Failure
            public data class Serialization(val path: String, val throwable: Throwable) : Failure
        }
    }
}

internal class DefaultSchemaParser : SchemaParser {
    @Suppress("ReturnCount")
    override fun schemaParserResult(schemaFile: File): SchemaParserResult {
        if (!schemaFile.exists()) return Failure.FileNotFound(schemaFile.path)

        val schemaFileContent = schemaFile.readText()
        if (schemaFileContent.isEmpty()) return Failure.FileIsEmpty(schemaFile.path)

        return try {
            val schemaDto = Json.decodeFromString<SchemaDto>(schemaFileContent)

            val verificationResult = schemaDto.isValid().toFailureOrNull(path = schemaFile.path)
            if (verificationResult != null) return verificationResult

            SchemaParserResult.Success(schemaDto.toSchema())
        } catch (exception: SerializationException) {
            Failure.Serialization(exception)
        }
    }

    //TODO: Test
    //TODO: Update processor
    override fun addSchema(
        schemaFile: File,
        newSchema: Schema
    ): AddSchemaResult {
        return try {
            if (!schemaFile.exists()) return AddSchemaResult.Failure.FileNotFound(schemaFile.path)
            if (schemaFile.isDirectory) return AddSchemaResult.Failure.InvalidFile(schemaFile.path)
            val verificationResult = newSchema.isValid().toFailureOrNull(schemaFile.path)
            if (verificationResult != null) return verificationResult

            val schemaDto = newSchema.toSchemaDto()
            val schemaFileContent = PrettyJson.encodeToString(schemaDto)
            schemaFile.writeText(schemaFileContent)

            AddSchemaResult.Success
        } catch (error: Exception) {
            AddSchemaResult.Failure.Serialization(schemaFile.path, error)
        }
    }

    private fun SchemaDto.ValidationResult.toFailureOrNull(path: String): Failure? =
        when (this) {
            VALID -> null
            EMPTY_SUPPORTED_PLATFORMS -> Failure.EmptySupportedPlatforms(path)
            EMPTY_PROPERTY_DEFINITIONS -> Failure.EmptyPropertyDefinitions(path)
            INVALID_PROPERTY_DEFINITION -> Failure.InvalidPropertyDefinition(path)
            DUPLICATED_PROPERTY_DEFINITION -> Failure.DuplicatedPropertyDefinition(path)
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