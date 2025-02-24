package io.github.gerardorodriguezdev.chamaleon.core.validators

import io.github.gerardorodriguezdev.chamaleon.core.models.*
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform.Property
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.BooleanProperty
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.StringProperty
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema.PropertyDefinition
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsProcessorResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsProcessorResult.Failure.*

internal fun Schema.areEnvironmentsValidOrFailure(
    environmentsDirectoryPath: String,
    environments: Set<Environment>
): Failure? =
    environments.firstNotNullOfOrNull { environment ->
        val context = Context(
            schema = this,
            environment = environment,
            environmentsDirectoryPath = environmentsDirectoryPath,
        )
        context.isEnvironmentValidOrFailure()
    }

private fun Context.isEnvironmentValidOrFailure(): Failure? {
    val environmentContainsAllPlatformsResult = environmentContainsAllPlatformsOrFailure()
    if (environmentContainsAllPlatformsResult is Failure) return environmentContainsAllPlatformsResult

    return environment.platforms.firstNotNullOfOrNull { platform ->
        isPlatformValidOrFailure(platform)
    }
}

private fun Context.environmentContainsAllPlatformsOrFailure(): Failure? {
    val platformTypes = environment.platformTypes()
    return if (schema.globalSupportedPlatformTypes != platformTypes) {
        EnvironmentMissingPlatforms(
            environmentsDirectoryPath = environmentsDirectoryPath,
            environmentName = environment.name,
            schemaPlatformTypes = schema.globalSupportedPlatformTypes,
            environmentPlatformTypes = platformTypes,
        )
    } else {
        null
    }
}

private fun Context.isPlatformValidOrFailure(platform: Platform): Failure? {
    val propertyDefinitionsForPlatform = propertyDefinitionsForPlatform(platform)

    val platformContainsAllPropertiesResult =
        platformContainsAllPropertiesOrFailure(
            propertyDefinitionsForPlatform = propertyDefinitionsForPlatform,
            platform = platform,
        )
    if (platformContainsAllPropertiesResult is Failure) return platformContainsAllPropertiesResult

    return platform.properties.firstNotNullOfOrNull { property ->
        isPropertyValidOrFailure(
            propertyDefinitionsForPlatform = propertyDefinitionsForPlatform,
            property = property,
            platformType = platform.platformType,
        )
    }
}

private fun Context.isPropertyValidOrFailure(
    propertyDefinitionsForPlatform: Set<PropertyDefinition>,
    property: Property,
    platformType: PlatformType,
): Failure? {
    val propertyDefinition = propertyDefinitionsForPlatform.propertyDefinition(property)

    return isPropertyTypeValidOrFailure(
        propertyDefinition = propertyDefinition,
        property = property,
        platformType = platformType,
    )
}

private fun Environment.platformTypes(): Set<PlatformType> = platforms.map { platform -> platform.platformType }.toSet()

private fun Context.propertyDefinitionsForPlatform(platform: Platform): Set<PropertyDefinition> =
    schema.propertyDefinitions
        .filter { propertyDefinition ->
            val platformType = platform.platformType
            if (propertyDefinition.supportedPlatformTypes.isEmpty()) {
                schema.globalSupportedPlatformTypes.contains(platformType)
            } else {
                propertyDefinition.supportedPlatformTypes.contains(platformType)
            }
        }.toSet()

private fun Context.platformContainsAllPropertiesOrFailure(
    propertyDefinitionsForPlatform: Set<PropertyDefinition>,
    platform: Platform,
): Failure? {
    val platformPropertiesNames = platform.properties.map { property -> property.name }
    val propertyDefinitionsNames = propertyDefinitionsForPlatform.map { property -> property.name }
    val platformContainsAllProperties = platformPropertiesNames == propertyDefinitionsNames

    return if (!platformContainsAllProperties) {
        PlatformMissingProperties(
            environmentsDirectoryPath = environmentsDirectoryPath,
            environmentName = environment.name,
            platformType = platform.platformType,
            schemaPropertyDefinitions = schema.propertyDefinitions,
            platformProperties = platform.properties,
        )
    } else {
        null
    }
}

private fun Set<PropertyDefinition>.propertyDefinition(property: Property): PropertyDefinition =
    first { propertyDefinition -> propertyDefinition.name == property.name }

private fun Context.isPropertyTypeValidOrFailure(
    propertyDefinition: PropertyDefinition,
    property: Property,
    platformType: PlatformType,
): Failure? {
    return when (property.value) {
        null -> isPropertyValueNullableOrFailure(
            propertyDefinition = propertyDefinition,
            propertyName = property.name,
            platformType = platformType,
        )

        else -> isPropertyTypeValidOrFailure(
            propertyName = property.name,
            propertyValue = property.value,
            propertyDefinition = propertyDefinition,
            platformType = platformType,
        )
    }
}

private fun Context.isPropertyValueNullableOrFailure(
    propertyDefinition: PropertyDefinition,
    propertyName: String,
    platformType: PlatformType,
): Failure? =
    if (!propertyDefinition.nullable) {
        NullPropertyNotNullable(
            propertyName = propertyName,
            platformType = platformType,
            environmentName = environment.name,
            environmentsDirectoryPath = environmentsDirectoryPath,
        )
    } else {
        null
    }

private fun Context.isPropertyTypeValidOrFailure(
    propertyName: String,
    propertyValue: PropertyValue,
    propertyDefinition: PropertyDefinition,
    platformType: PlatformType,
): Failure? {
    val propertyType = propertyValue.toPropertyType()

    return if (propertyDefinition.propertyType != propertyType) {
        PropertyTypeNotEqualToPropertyDefinition(
            propertyName = propertyName,
            platformType = platformType,
            environmentName = environment.name,
            propertyType = propertyType,
            environmentsDirectoryPath = environmentsDirectoryPath,
        )
    } else {
        null
    }
}

private fun PropertyValue.toPropertyType(): PropertyType =
    when (this) {
        is StringProperty -> PropertyType.STRING
        is BooleanProperty -> PropertyType.BOOLEAN
    }

private data class Context(
    val schema: Schema,
    val environment: Environment,
    val environmentsDirectoryPath: String,
)