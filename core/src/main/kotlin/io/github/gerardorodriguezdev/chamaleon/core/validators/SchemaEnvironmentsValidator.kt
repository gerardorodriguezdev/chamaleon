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
    environmentsMap: Map<String, Environment>
): Failure? =
    environmentsMap.values.firstNotNullOfOrNull { environment ->
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

    return environment.platformsMap.values.firstNotNullOfOrNull { platform ->
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
    val propertyDefinitionsForPlatformMap = propertyDefinitionsForPlatformMap(platform)

    val platformContainsAllPropertiesResult =
        platformContainsAllPropertiesOrFailure(
            propertyDefinitionsForPlatformMap = propertyDefinitionsForPlatformMap,
            platform = platform,
        )
    if (platformContainsAllPropertiesResult is Failure) return platformContainsAllPropertiesResult

    return platform.propertiesMap.values.firstNotNullOfOrNull { property ->
        isPropertyValidOrFailure(
            propertyDefinitionsForPlatformMap = propertyDefinitionsForPlatformMap,
            property = property,
            platformType = platform.platformType,
        )
    }
}

private fun Context.isPropertyValidOrFailure(
    propertyDefinitionsForPlatformMap: Map<String, PropertyDefinition>,
    property: Property,
    platformType: PlatformType,
): Failure? {
    val propertyDefinition = propertyDefinitionsForPlatformMap.propertyDefinition(property)

    return isPropertyTypeValidOrFailure(
        propertyDefinition = propertyDefinition,
        property = property,
        platformType = platformType,
    )
}

private fun Environment.platformTypes(): Set<PlatformType> = platformsMap.keys

private fun Context.propertyDefinitionsForPlatformMap(platform: Platform): Map<String, PropertyDefinition> =
    schema.propertyDefinitionsMap.values
        .filter { propertyDefinition ->
            val platformType = platform.platformType
            if (propertyDefinition.supportedPlatformTypes.isEmpty()) {
                schema.globalSupportedPlatformTypes.contains(platformType)
            } else {
                propertyDefinition.supportedPlatformTypes.contains(platformType)
            }
        }
        .associateBy { propertyDefinition -> propertyDefinition.name }

private fun Context.platformContainsAllPropertiesOrFailure(
    propertyDefinitionsForPlatformMap: Map<String, PropertyDefinition>,
    platform: Platform,
): Failure? {
    val platformPropertiesNames = platform.propertiesMap.keys
    val propertyDefinitionsNames = propertyDefinitionsForPlatformMap.keys
    val platformContainsAllProperties = platformPropertiesNames == propertyDefinitionsNames

    return if (!platformContainsAllProperties) {
        PlatformMissingProperties(
            environmentsDirectoryPath = environmentsDirectoryPath,
            environmentName = environment.name,
            platformType = platform.platformType,
            schemaPropertyDefinitions = schema.propertyDefinitionsMap.values.toSet(),
            platformProperties = platform.propertiesMap.values.toSet(),
        )
    } else {
        null
    }
}

private fun Map<String, PropertyDefinition>.propertyDefinition(property: Property): PropertyDefinition =
    getValue(property.name)

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

private class Context(
    val schema: Schema,
    val environment: Environment,
    val environmentsDirectoryPath: String,
)