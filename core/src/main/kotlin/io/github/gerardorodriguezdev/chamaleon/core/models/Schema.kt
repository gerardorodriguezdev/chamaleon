package io.github.gerardorodriguezdev.chamaleon.core.models

public data class Schema(
    val globalSupportedPlatformTypes: Set<PlatformType> = emptySet(),
    val propertyDefinitionsMap: Map<String, PropertyDefinition> = emptyMap(),
) {
    public data class PropertyDefinition(
        val name: String,
        val propertyType: PropertyType,
        val nullable: Boolean,
        val supportedPlatformTypes: Set<PlatformType>,
    )
}