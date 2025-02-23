package io.github.gerardorodriguezdev.chamaleon.core.validators

import io.github.gerardorodriguezdev.chamaleon.core.models.*
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform.Property
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.BooleanProperty
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.StringProperty
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema.PropertyDefinition
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsProcessorResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsProcessorResult.Failure.*

//TODO: Binds
internal fun Schema.areEnvironmentsValidOrFailure(
    environmentsDirectoryPath: String,
    environments: Set<Environment>
): Failure? {
    environments.forEach { environment ->
        val environmentContainsAllPlatformsResult = environmentContainsAllPlatformsOrFailure(
            environmentsDirectoryPath = environmentsDirectoryPath,
            environment = environment,
        )
        if (environmentContainsAllPlatformsResult is Failure) return environmentContainsAllPlatformsResult

        environment.platforms.forEach { platform ->
            val propertyDefinitionsForPlatform = propertyDefinitionsForPlatform(platform)

            val platformContainsAllPropertiesResult =
                platformContainsAllPropertiesOrFailure(
                    propertyDefinitionsForPlatform = propertyDefinitionsForPlatform,
                    platform = platform,
                    environmentName = environment.name,
                    environmentsDirectoryPath = environmentsDirectoryPath,
                )
            if (platformContainsAllPropertiesResult is Failure) return platformContainsAllPropertiesResult

            platform.properties.forEach { property ->
                val propertyDefinition = propertyDefinitionsForPlatform.propertyDefinition(property)

                val isPropertyTypeValidResult =
                    isPropertyTypeValidOrFailure(
                        propertyDefinition = propertyDefinition,
                        property = property,
                        platformType = platform.platformType,
                        environmentName = environment.name,
                        environmentsDirectoryPath = environmentsDirectoryPath
                    )
                if (isPropertyTypeValidResult is Failure) return isPropertyTypeValidResult
            }
        }
    }

    return null
}

private fun Schema.environmentContainsAllPlatformsOrFailure(
    environmentsDirectoryPath: String,
    environment: Environment
): Failure? {
    val platformTypes = environment.platformTypes()
    return if (globalSupportedPlatformTypes != platformTypes) {
        EnvironmentMissingPlatforms(
            environmentsDirectoryPath = environmentsDirectoryPath,
            environmentName = environment.name,
            schemaPlatformTypes = globalSupportedPlatformTypes,
            environmentPlatformTypes = platformTypes,
        )
    } else null
}

private fun Environment.platformTypes(): Set<PlatformType> =
    platforms.map { platform -> platform.platformType }.toSet()

private fun Schema.propertyDefinitionsForPlatform(platform: Platform): Set<PropertyDefinition> =
    propertyDefinitions
        .filter { propertyDefinition ->
            val platformType = platform.platformType
            if (propertyDefinition.supportedPlatformTypes.isEmpty()) {
                globalSupportedPlatformTypes.contains(platformType)
            } else {
                propertyDefinition.supportedPlatformTypes.contains(platformType)
            }
        }.toSet()

private fun Schema.platformContainsAllPropertiesOrFailure(
    propertyDefinitionsForPlatform: Set<PropertyDefinition>,
    platform: Platform,
    environmentName: String,
    environmentsDirectoryPath: String,
): Failure? {
    val platformPropertiesNames = platform.properties.map { property -> property.name }
    val propertyDefinitionsNames = propertyDefinitionsForPlatform.map { property -> property.name }
    val platformContainsAllProperties = platformPropertiesNames == propertyDefinitionsNames

    return if (!platformContainsAllProperties) {
        PlatformMissingProperties(
            environmentsDirectoryPath = environmentsDirectoryPath,
            environmentName = environmentName,
            platformType = platform.platformType,
            schemaPropertyDefinitions = propertyDefinitions,
            platformProperties = platform.properties,
        )
    } else null
}

private fun Set<PropertyDefinition>.propertyDefinition(property: Property): PropertyDefinition =
    first { propertyDefinition -> propertyDefinition.name == property.name }

private fun isPropertyTypeValidOrFailure(
    propertyDefinition: PropertyDefinition,
    property: Property,
    platformType: PlatformType,
    environmentName: String,
    environmentsDirectoryPath: String,
): Failure? {
    return when (property.value) {
        null -> isPropertyValueNullableOrFailure(
            propertyDefinition = propertyDefinition,
            propertyName = property.name,
            platformType = platformType,
            environmentName = environmentName,
            environmentsDirectoryPath = environmentsDirectoryPath,
        )

        else -> isPropertyTypeValidOrFailure(
            propertyName = property.name,
            propertyValue = property.value,
            propertyDefinition = propertyDefinition,
            platformType = platformType,
            environmentName = environmentName,
            environmentsDirectoryPath = environmentsDirectoryPath,
        )
    }
}

private fun isPropertyValueNullableOrFailure(
    propertyDefinition: PropertyDefinition,
    propertyName: String,
    platformType: PlatformType,
    environmentName: String,
    environmentsDirectoryPath: String,
): Failure? =
    if (!propertyDefinition.nullable) {
        NullPropertyNotNullable(
            propertyName = propertyName,
            platformType = platformType,
            environmentName = environmentName,
            environmentsDirectoryPath = environmentsDirectoryPath,
        )
    } else null

private fun isPropertyTypeValidOrFailure(
    propertyName: String,
    propertyValue: PropertyValue,
    propertyDefinition: PropertyDefinition,
    platformType: PlatformType,
    environmentName: String,
    environmentsDirectoryPath: String,
): Failure? {
    val propertyType = propertyValue.toPropertyType()

    return if (propertyDefinition.propertyType != propertyType) {
        PropertyTypeNotEqualToPropertyDefinition(
            propertyName = propertyName,
            platformType = platformType,
            environmentName = environmentName,
            propertyType = propertyType,
            environmentsDirectoryPath = environmentsDirectoryPath,
        )
    } else null
}

private fun PropertyValue.toPropertyType(): PropertyType =
    when (this) {
        is StringProperty -> PropertyType.STRING
        is BooleanProperty -> PropertyType.BOOLEAN
    }