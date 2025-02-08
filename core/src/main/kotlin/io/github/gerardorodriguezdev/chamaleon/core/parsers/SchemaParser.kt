package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.dtos.SchemaDto
import io.github.gerardorodriguezdev.chamaleon.core.dtos.SchemaDto.PropertyDefinitionDto
import io.github.gerardorodriguezdev.chamaleon.core.dtos.SchemaDto.ValidationResult.*
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema.PropertyDefinition
import io.github.gerardorodriguezdev.chamaleon.core.parsers.SchemaParser.SchemaParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.SchemaParser.SchemaParserResult.Failure
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File

public interface SchemaParser {
    public fun schemaParserResult(schemaFile: File): SchemaParserResult

    public sealed interface SchemaParserResult {
        public data class Success(val schema: Schema) : SchemaParserResult

        //TODO: Take out
        public sealed interface Failure : SchemaParserResult {
            public data class FileNotFound(val path: String) : Failure
            public data class FileIsEmpty(val path: String) : Failure
            public data class Serialization(val throwable: Throwable) : Failure
            public data class EmptySupportedPlatforms(val path: String) : Failure
            public data class EmptyPropertyDefinitions(val path: String) : Failure
            public data class InvalidPropertyDefinition(val path: String) : Failure
            public data class DuplicatedPropertyDefinition(val path: String) : Failure
        }
    }
}

internal class DefaultSchemaParser : SchemaParser {
    @Suppress("ReturnCount")
    override fun schemaParserResult(schemaFile: File): SchemaParserResult {
        if (!schemaFile.exists()) return Failure.FileNotFound(schemaFile.parent)

        val schemaFileContent = schemaFile.readText()
        if (schemaFileContent.isEmpty()) return Failure.FileIsEmpty(schemaFile.parent)

        return try {
            val schemaDto = Json.decodeFromString<SchemaDto>(schemaFileContent)

            val verificationResult = schemaDto.isValid().toFailureOrNull(path = schemaFile.parent)
            if (verificationResult != null) return verificationResult

            SchemaParserResult.Success(schemaDto.toSchema())
        } catch (exception: SerializationException) {
            Failure.Serialization(exception)
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

    private fun SchemaDto.toSchema(): Schema =
        Schema(
            supportedPlatforms = this@toSchema.supportedPlatforms,
            propertyDefinitions = propertyDefinitionDtos.toPropertyDefinitions(),
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
}