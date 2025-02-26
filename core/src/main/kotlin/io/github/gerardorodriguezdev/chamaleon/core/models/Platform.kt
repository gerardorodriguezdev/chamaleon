package io.github.gerardorodriguezdev.chamaleon.core.models

import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.BooleanProperty
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.StringProperty

public data class Platform(
    val platformType: PlatformType,
    val propertiesMap: Map<String, Property>,
) {
    public fun propertyStringValue(name: String): String {
        val property = propertiesMap.getValue(name)
        val stringProperty = property.value as StringProperty
        return stringProperty.value
    }

    public fun propertyStringValueOrNull(name: String): String? {
        val property = propertiesMap[name]
        val stringProperty = property?.value as? StringProperty
        return stringProperty?.value
    }

    public fun propertyBooleanValue(name: String): Boolean {
        val property = propertiesMap.getValue(name)
        val stringProperty = property.value as BooleanProperty
        return stringProperty.value
    }

    public fun propertyBooleanValueOrNull(name: String): Boolean? {
        val property = propertiesMap[name]
        val stringProperty = property?.value as? BooleanProperty
        return stringProperty?.value
    }

    public data class Property(
        val name: String,
        val value: PropertyValue?,
    )
}