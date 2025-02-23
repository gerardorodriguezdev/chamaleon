package io.github.gerardorodriguezdev.chamaleon.core.entities

public data class Schema(
    val globalSupportedPlatformTypes: Set<PlatformType> = emptySet(),
    val propertyDefinitions: Set<PropertyDefinition> = emptySet(),
) {
    public data class PropertyDefinition(
        val name: String,
        val propertyType: PropertyType,
        val nullable: Boolean,
        val supportedPlatformTypes: Set<PlatformType>,
    )
}