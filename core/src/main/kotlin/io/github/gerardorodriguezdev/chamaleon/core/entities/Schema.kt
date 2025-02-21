package io.github.gerardorodriguezdev.chamaleon.core.entities

import io.github.gerardorodriguezdev.chamaleon.core.entities.Platform.Property
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyValue.BooleanProperty
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyValue.StringProperty
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema.EnvironmentsValidationResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema.EnvironmentsValidationResult.Failure.*

@Suppress("ReturnCount")
public data class Schema(
    val globalSupportedPlatforms: Set<PlatformType> = emptySet(),
    val propertyDefinitions: Set<PropertyDefinition> = emptySet(),
) {
    public data class PropertyDefinition(
        val name: String,
        val propertyType: PropertyType,
        val nullable: Boolean,
        val supportedPlatforms: Set<PlatformType>,
    ) {
        internal fun isValid(): Boolean = name.isNotEmpty()
    }

    internal fun isValid(): ValidationResult {
        if (this@Schema.globalSupportedPlatforms.isEmpty()) return ValidationResult.EMPTY_SUPPORTED_PLATFORMS
        if (propertyDefinitions.isEmpty()) return ValidationResult.EMPTY_PROPERTY_DEFINITIONS
        if (propertyDefinitions.any { propertyDefinition -> !propertyDefinition.isValid() }) {
            return ValidationResult.INVALID_PROPERTY_DEFINITION
        }

        val uniquePropertyDefinitions =
            propertyDefinitions.distinctBy { propertyDefinition -> propertyDefinition.name }
        if (propertyDefinitions.size != uniquePropertyDefinitions.size) {
            return ValidationResult.DUPLICATED_PROPERTY_DEFINITION
        }

        return ValidationResult.VALID
    }

    internal enum class ValidationResult {
        VALID,
        EMPTY_SUPPORTED_PLATFORMS,
        EMPTY_PROPERTY_DEFINITIONS,
        INVALID_PROPERTY_DEFINITION,
        DUPLICATED_PROPERTY_DEFINITION,
    }

    internal fun environmentsValidationResults(environments: Set<Environment>): List<EnvironmentsValidationResult> =
        environments.map { environment ->
            val verifyEnvironmentContainsAllPlatformsResult = verifyEnvironmentContainsAllPlatforms(environment)
            if (verifyEnvironmentContainsAllPlatformsResult is Failure) {
                return@map verifyEnvironmentContainsAllPlatformsResult
            }

            environment.platforms.forEach { platform ->
                val verifyPlatformContainsAllPropertiesResult =
                    verifyPlatformContainsAllProperties(platform, environment.name)
                if (verifyPlatformContainsAllPropertiesResult is Failure) {
                    return@map verifyPlatformContainsAllPropertiesResult
                }

                platform.properties.forEach { property ->
                    val verifyPropertyTypeIsCorrectResult =
                        verifyPropertyTypeIsCorrect(property, platform.platformType, environment.name)
                    if (verifyPropertyTypeIsCorrectResult is Failure) return@map verifyPropertyTypeIsCorrectResult
                }
            }

            EnvironmentsValidationResult.Success
        }

    private fun Schema.verifyEnvironmentContainsAllPlatforms(environment: Environment): EnvironmentsValidationResult? {
        val platformTypes = environment.platforms.map { platform -> platform.platformType }

        return if (!containsAll(platformTypes)) PlatformsNotEqualToSchema(environment.name) else null
    }

    private fun Schema.containsAll(platformTypes: List<PlatformType>): Boolean =
        this@containsAll.globalSupportedPlatforms.size == platformTypes.size && this@containsAll.globalSupportedPlatforms.containsAll(
            platformTypes
        )

    private fun Schema.verifyPlatformContainsAllProperties(
        platform: Platform,
        environmentName: String
    ): EnvironmentsValidationResult? {
        val propertiesNotEqualToSchema = PropertiesNotEqualToSchema(platform.platformType, environmentName)
        if (isPlatformNotSupported(platform)) return propertiesNotEqualToSchema
        if (platformHasMorePropertiesThanSchema(platform)) return propertiesNotEqualToSchema

        propertyDefinitions.forEach { propertyDefinition ->
            if (!propertyDefinition.verify(platform)) return propertiesNotEqualToSchema
        }

        return null
    }

    private fun Schema.platformHasMorePropertiesThanSchema(platform: Platform): Boolean =
        propertyDefinitions.size < platform.properties.size

    private fun Set<Property>.contains(propertyDefinition: PropertyDefinition): Boolean =
        any { property -> property.name == propertyDefinition.name }

    private fun PropertyDefinition.verify(platform: Platform): Boolean {
        val isPlatformSupported = supportedPlatforms.isEmpty() || supportedPlatforms.contains(platform.platformType)
        val isPropertyPresent = platform.properties.contains(this)
        return isPlatformSupported == isPropertyPresent
    }

    private fun Schema.isPlatformNotSupported(platform: Platform): Boolean =
        platform.platformType !in this@isPlatformNotSupported.globalSupportedPlatforms

    private fun Schema.verifyPropertyTypeIsCorrect(
        property: Property,
        platformType: PlatformType,
        environmentName: String,
    ): EnvironmentsValidationResult? {
        val propertyDefinitions =
            propertyDefinitions.first { propertyDefinition -> propertyDefinition.name == property.name }

        return when (property.value) {
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
    ): EnvironmentsValidationResult? =
        if (!propertyDefinition.nullable) {
            NullPropertyNotNullableOnSchema(
                propertyName = propertyName,
                platformType = platformType,
                environmentName = environmentName,
            )
        } else {
            null
        }

    private fun verifyPropertyType(
        propertyName: String,
        propertyValue: PropertyValue,
        propertyDefinition: PropertyDefinition,
        platformType: PlatformType,
        environmentName: String
    ): EnvironmentsValidationResult? {
        val propertyType = propertyValue.toPropertyType()

        return if (propertyDefinition.propertyType != propertyType) {
            PropertyTypeNotMatchSchema(
                propertyName = propertyName,
                platformType = platformType,
                environmentName = environmentName,
                propertyType = propertyType,
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

    public sealed interface EnvironmentsValidationResult {
        public data object Success : EnvironmentsValidationResult
        public sealed interface Failure : EnvironmentsValidationResult {
            public data class PlatformsNotEqualToSchema(val environmentName: String) : Failure
            public data class PropertiesNotEqualToSchema(
                val platformType: PlatformType,
                val environmentName: String
            ) : Failure

            public data class PropertyTypeNotMatchSchema(
                val propertyName: String,
                val platformType: PlatformType,
                val environmentName: String,
                val propertyType: PropertyType,
            ) : Failure

            public data class NullPropertyNotNullableOnSchema(
                val propertyName: String,
                val platformType: PlatformType,
                val environmentName: String,
            ) : Failure
        }
    }
}