package io.github.gerardorodriguezdev.chamaleon.core.entities

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
        //TODO: Test
        internal fun isValid(): Boolean = name.isNotEmpty()
    }

    //TODO: Test
    internal fun isValid(): ValidationResult {
        if (supportedPlatforms.isEmpty()) return ValidationResult.EMPTY_PLATFORMS
        if (propertyDefinitions.isEmpty()) return ValidationResult.EMPTY_PROPERTY_DEFINITIONS
        if (propertyDefinitions.any { propertyDefinition -> !propertyDefinition.isValid() }) {
            return ValidationResult.INVALID_PROPERTY_DEFINITION
        }

        return ValidationResult.VALID
    }

    internal enum class ValidationResult {
        VALID,
        EMPTY_PLATFORMS,
        EMPTY_PROPERTY_DEFINITIONS,
        INVALID_PROPERTY_DEFINITION,
    }
}