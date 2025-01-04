package io.github.gerardorodriguezdev.chamaleon.core.models

public data class Schema(
    val supportedPlatforms: Set<PlatformType>,
    val propertyDefinitions: Set<PropertyDefinition>,
) {
    public data class PropertyDefinition(
        val name: String,
        val propertyType: PropertyType,
        val nullable: Boolean,
    )
}
