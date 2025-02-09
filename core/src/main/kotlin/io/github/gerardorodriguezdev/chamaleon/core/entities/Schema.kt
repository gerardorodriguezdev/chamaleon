package io.github.gerardorodriguezdev.chamaleon.core.entities

@Suppress("ReturnCount")
public data class Schema(
    val supportedPlatforms: Set<PlatformType>,
    val propertyDefinitions: Set<PropertyDefinition>,
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
        if (supportedPlatforms.isEmpty()) return ValidationResult.EMPTY_SUPPORTED_PLATFORMS
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
}