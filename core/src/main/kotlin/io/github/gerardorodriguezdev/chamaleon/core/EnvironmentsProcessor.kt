package io.github.gerardorodriguezdev.chamaleon.core

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.EnvironmentsProcessorException.*
import io.github.gerardorodriguezdev.chamaleon.core.models.*
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform.Property
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.BooleanProperty
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.StringProperty
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema.PropertyDefinition
import io.github.gerardorodriguezdev.chamaleon.core.parsers.DefaultEnvironmentsParser
import io.github.gerardorodriguezdev.chamaleon.core.parsers.DefaultSchemaParser
import io.github.gerardorodriguezdev.chamaleon.core.parsers.EnvironmentsParser
import io.github.gerardorodriguezdev.chamaleon.core.parsers.EnvironmentsParser.EnvironmentsParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.SchemaParser
import io.github.gerardorodriguezdev.chamaleon.core.parsers.SchemaParser.SchemaParserResult
import java.io.File

class EnvironmentsProcessor(
    val schemaParser: SchemaParser,
    val environmentsParser: EnvironmentsParser,
) {
    constructor(directory: File) : this(
        schemaParser = DefaultSchemaParser(directory, SCHEMA_FILE),
        environmentsParser = DefaultEnvironmentsParser(directory),
    )

    fun environments(): Set<Environment> {
        val schemaParsingResult = schemaParser.schemaParserResult()
        val schema = schemaParsingResult.schema()

        val environmentsParserResult = environmentsParser.environmentsParserResult()
        val environments = environmentsParserResult.environments()

        schema.verifyEnvironments(environments)

        return environments
    }

    private fun SchemaParserResult.schema(): Schema =
        when (this) {
            is SchemaParserResult.Success -> schema
            is SchemaParserResult.Failure.FileNotFound -> throw SchemaFileNotFound(path)
            is SchemaParserResult.Failure.FileIsEmpty -> throw SchemaFileIsEmpty(path)
            is SchemaParserResult.Failure.SerializationError -> throw throwable
        }

    private fun EnvironmentsParserResult.environments(): Set<Environment> =
        when (this) {
            is EnvironmentsParserResult.Success -> environments
            is EnvironmentsParserResult.Failure.SerializationError -> throw throwable
        }

    private fun Schema.verifyEnvironments(environments: Set<Environment>) {
        environments.forEach { environment ->
            verifyEnvironmentContainsAllPlatforms(environment)

            environment.platforms.forEach { platform ->
                verifyPlatformContainsAllProperties(platform, environment.name)

                platform.properties.forEach { property ->
                    verifyPropertyTypeIsCorrect(property, platform.platformType, environment.name)
                }
            }
        }
    }

    private fun Schema.verifyEnvironmentContainsAllPlatforms(environment: Environment) {
        val platformTypes = environment.platforms.map { platform -> platform.platformType }

        if (supportedPlatforms.size != platformTypes.size || !supportedPlatforms.containsAll(platformTypes)) {
            throw PlatformsNotEqualToSchema(environment.name)
        }
    }

    private fun Schema.verifyPlatformContainsAllProperties(platform: Platform, environmentName: String) {
        val propertyDefinitionNames = propertyDefinitions.map { propertyDefinition -> propertyDefinition.name }
        val propertyNames = platform.properties.map { property -> property.name }

        if (propertyDefinitionNames.size != propertyNames.size || !propertyDefinitionNames.containsAll(propertyNames)) {
            throw PropertiesNotEqualToSchema(platform.platformType, environmentName)
        }
    }

    private fun Schema.verifyPropertyTypeIsCorrect(
        property: Property,
        platformType: PlatformType,
        environmentName: String,
    ) {
        val propertyDefinitions =
            propertyDefinitions.first { propertyDefinition -> propertyDefinition.name == property.name }

        when (property.value) {
            null -> verifyNullPropertyValue(
                propertyDefinition = propertyDefinitions,
                propertyName = property.name,
                platformType = platformType,
                environmentName = environmentName,
            )

            else -> verifyPropertyType(
                propertyName = property.name,
                propertyValue = property.value,
                propertyDefinition = propertyDefinitions,
                platformType = platformType,
                environmentName = environmentName,
            )
        }
    }

    private fun verifyNullPropertyValue(
        propertyDefinition: PropertyDefinition,
        propertyName: String,
        platformType: PlatformType,
        environmentName: String,
    ) {
        if (!propertyDefinition.nullable) {
            throw NullPropertyNotNullableOnSchema(
                propertyName = propertyName,
                platformType = platformType,
                environmentName = environmentName,
            )
        }
    }

    private fun verifyPropertyType(
        propertyName: String,
        propertyValue: PropertyValue,
        propertyDefinition: PropertyDefinition,
        platformType: PlatformType,
        environmentName: String
    ) {
        val propertyType = propertyValue.toPropertyType()

        if (propertyDefinition.propertyType != propertyType) {
            throw PropertyTypeNotMatchSchema(
                propertyName = propertyName,
                platformType = platformType,
                environmentName = environmentName,
                propertyType = propertyType,
            )
        }
    }

    private fun PropertyValue.toPropertyType(): PropertyType =
        when (this) {
            is StringProperty -> PropertyType.STRING
            is BooleanProperty -> PropertyType.BOOLEAN
        }

    sealed class EnvironmentsProcessorException(message: String) : Exception(message) {
        class SchemaFileNotFound(directoryPath: String) :
            EnvironmentsProcessorException("$SCHEMA_FILE not found on $directoryPath")

        class SchemaFileIsEmpty(directoryPath: String) :
            EnvironmentsProcessorException("$SCHEMA_FILE on $directoryPath is empty")

        class PlatformsNotEqualToSchema(environmentName: String) :
            EnvironmentsProcessorException("Platforms of environment $environmentName are not equal to schema")

        class PropertiesNotEqualToSchema(platformType: PlatformType, environmentName: String) :
            EnvironmentsProcessorException(
                "Properties on platform $platformType for environment $environmentName are not equal to schema"
            )

        @Suppress("Indentation")
        class PropertyTypeNotMatchSchema(
            propertyName: String,
            platformType: PlatformType,
            environmentName: String,
            propertyType: PropertyType,
        ) : EnvironmentsProcessorException(
            "Value of property $propertyName for platform $platformType " +
                    "on environment $environmentName doesn't match propertyType $propertyType on schema"
        )

        @Suppress("Indentation")
        class NullPropertyNotNullableOnSchema(
            propertyName: String,
            platformType: PlatformType,
            environmentName: String,
        ) : EnvironmentsProcessorException(
            "Value on property $propertyName for platform $platformType on environment " +
                    "$environmentName was null and is not marked as nullable on schema"
        )
    }

    private companion object {
        const val SCHEMA_FILE = "cha.json"
    }
}