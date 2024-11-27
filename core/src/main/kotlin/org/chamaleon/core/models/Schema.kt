package org.chamaleon.core.models

data class Schema(
    val supportedPlatforms: Set<PlatformType>,
    val propertyDefinitions: Set<PropertyDefinition>,
) {
    data class PropertyDefinition(
        val name: String,
        val propertyType: PropertyType,
        val nullable: Boolean,
    )
}
