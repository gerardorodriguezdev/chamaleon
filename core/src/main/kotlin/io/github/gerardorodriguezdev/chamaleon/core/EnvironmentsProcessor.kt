package io.github.gerardorodriguezdev.chamaleon.core

import io.github.gerardorodriguezdev.chamaleon.core.DefaultEnvironmentsProcessor.DefaultEnvironmentsProcessorException.*
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.EnvironmentsProcessorResult
import io.github.gerardorodriguezdev.chamaleon.core.models.*
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform.Property
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.BooleanProperty
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.StringProperty
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema.PropertyDefinition
import io.github.gerardorodriguezdev.chamaleon.core.parsers.*
import io.github.gerardorodriguezdev.chamaleon.core.parsers.EnvironmentsParser.EnvironmentsParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser.PropertiesParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.SchemaParser.SchemaParserResult
import java.io.File

interface EnvironmentsProcessor {
    fun process(directory: File): EnvironmentsProcessorResult
    fun updateSelectedEnvironment(propertiesFile: File, newSelectedEnvironment: String?): Boolean

    data class EnvironmentsProcessorResult(
        val selectedEnvironmentName: String? = null,
        val environments: Set<Environment>,
    )

    companion object {
        const val CONVENTION_ENVIRONMENTS_DIRECTORY_NAME = "environments"
    }
}

class DefaultEnvironmentsProcessor(
    val schemaParser: SchemaParser = DefaultSchemaParser(),
    val environmentsParser: EnvironmentsParser = DefaultEnvironmentsParser(),
    val propertiesParser: PropertiesParser = DefaultPropertiesParser(),
) : EnvironmentsProcessor {

    override fun process(directory: File): EnvironmentsProcessorResult {
        val schemaFile = File(directory, SCHEMA_FILE)
        val schemaParsingResult = schemaParser.schemaParserResult(schemaFile)
        val schema = schemaParsingResult.schema()

        val environmentsParserResult = environmentsParser.environmentsParserResult(directory)
        val environments = environmentsParserResult.environments()

        val propertiesFile = File(directory, PROPERTIES_FILE)
        val propertiesParserResult = propertiesParser.propertiesParserResult(propertiesFile)
        val selectedEnvironmentName = propertiesParserResult.selectedEnvironmentName()

        schema.verifyEnvironments(environments)
        selectedEnvironmentName.verifyEnvironments(environments)

        return EnvironmentsProcessorResult(
            selectedEnvironmentName = selectedEnvironmentName,
            environments = environments,
        )
    }

    override fun updateSelectedEnvironment(directory: File, newSelectedEnvironment: String?): Boolean =
        propertiesParser.updateSelectedEnvironment(File(directory, PROPERTIES_FILE), newSelectedEnvironment)

    private fun SchemaParserResult.schema(): Schema =
        when (this) {
            is SchemaParserResult.Success -> schema
            is SchemaParserResult.Failure.FileNotFound -> throw SchemaFileNotFound(path)
            is SchemaParserResult.Failure.FileIsEmpty -> throw SchemaFileIsEmpty(path)
            is SchemaParserResult.Failure.Serialization -> throw throwable
        }

    private fun EnvironmentsParserResult.environments(): Set<Environment> =
        when (this) {
            is EnvironmentsParserResult.Success -> environments
            is EnvironmentsParserResult.Failure.Serialization -> throw throwable
        }

    private fun PropertiesParserResult.selectedEnvironmentName(): String? =
        when (this) {
            is PropertiesParserResult.Success -> selectedEnvironmentName
            is PropertiesParserResult.Failure -> throw throwable
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

    private fun String?.verifyEnvironments(environments: Set<Environment>) {
        if (this != null) {
            if (!environments.any { environment -> environment.name == this })
                throw SelectedEnvironmentInvalid(
                    selectedEnvironmentName = this,
                    environmentNames = environments.joinToString { environment -> environment.name }
                )
        }
    }

    private fun PropertyValue.toPropertyType(): PropertyType =
        when (this) {
            is StringProperty -> PropertyType.STRING
            is BooleanProperty -> PropertyType.BOOLEAN
        }

    sealed class DefaultEnvironmentsProcessorException(message: String) : Exception(message) {
        class SchemaFileNotFound(directoryPath: String) :
            DefaultEnvironmentsProcessorException("'$SCHEMA_FILE' not found on '$directoryPath'")

        class SchemaFileIsEmpty(directoryPath: String) :
            DefaultEnvironmentsProcessorException("'$SCHEMA_FILE' on '$directoryPath' is empty")

        class PlatformsNotEqualToSchema(environmentName: String) :
            DefaultEnvironmentsProcessorException("Platforms of environment '$environmentName' are not equal to schema")

        class PropertiesNotEqualToSchema(platformType: PlatformType, environmentName: String) :
            DefaultEnvironmentsProcessorException(
                "Properties on platform '$platformType' for environment '$environmentName' are not equal to schema"
            )

        @Suppress("Indentation")
        class PropertyTypeNotMatchSchema(
            propertyName: String,
            platformType: PlatformType,
            environmentName: String,
            propertyType: PropertyType,
        ) : DefaultEnvironmentsProcessorException(
            "Value of property '$propertyName' for platform '$platformType' " +
                    "on environment '$environmentName' doesn't match propertyType '$propertyType' on schema"
        )

        @Suppress("Indentation")
        class NullPropertyNotNullableOnSchema(
            propertyName: String,
            platformType: PlatformType,
            environmentName: String,
        ) : DefaultEnvironmentsProcessorException(
            "Value on property '$propertyName' for platform '$platformType' on environment " +
                    "'$environmentName' was null and is not marked as nullable on schema"
        )

        @Suppress("Indentation")
        class SelectedEnvironmentInvalid(selectedEnvironmentName: String, environmentNames: String) :
            DefaultEnvironmentsProcessorException(
                "Selected environment '$selectedEnvironmentName' on '$PROPERTIES_FILE' not present in any environment" +
                        "[$environmentNames]"
            )
    }

    private companion object {
        const val SCHEMA_FILE = "cha.json"
        const val PROPERTIES_FILE = "cha.properties.json"
    }
}