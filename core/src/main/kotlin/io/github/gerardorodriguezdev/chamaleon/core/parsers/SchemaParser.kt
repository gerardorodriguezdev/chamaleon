package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.dtos.SchemaDto
import io.github.gerardorodriguezdev.chamaleon.core.dtos.SchemaDto.PropertyDefinitionDto
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
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

        public sealed interface Failure : SchemaParserResult {
            public data class FileNotFound(val path: String) : Failure
            public data class FileIsEmpty(val path: String) : Failure
            public data class Serialization(val throwable: Throwable) : Failure
            public data class PropertyContainsUnsupportedPlatforms(
                val path: String,
                val propertyName: String,
            ) : Failure
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

            val verificationResult = schemaDto.verify(path = schemaFile.parent)
            if (verificationResult != null) return verificationResult

            SchemaParserResult.Success(schemaDto.toSchema())
        } catch (exception: SerializationException) {
            Failure.Serialization(exception)
        }
    }

    private fun SchemaDto.verify(path: String): Failure? {
        // TODO: No empty definitions
        // TODO: No empty name for prop def
        // TODO: No dup name
        propertyDefinitionDtos
            .forEach { propertyDefinitionDto ->
                if (propertyDefinitionDto.containsUnsupportedPlatforms(this@verify.supportedPlatforms)) {
                    return Failure.PropertyContainsUnsupportedPlatforms(
                        path = path,
                        propertyName = propertyDefinitionDto.name,
                    )
                }
            }

        return null
    }

    private fun PropertyDefinitionDto.containsUnsupportedPlatforms(supportedPlatforms: Set<PlatformType>): Boolean =
        this.supportedPlatforms.isNotEmpty() && !supportedPlatforms.containsAll(this.supportedPlatforms)

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