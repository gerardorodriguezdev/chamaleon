package io.github.gerardorodriguezdev.chamaleon.core.entities

import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyValue.BooleanProperty
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyValue.StringProperty

public data class Platform(
    val platformType: PlatformType,
    val properties: Set<Property>,
) {
    public fun propertyStringValue(name: String): String {
        val property = properties.first { property -> property.name == name }
        val stringProperty = property.value as StringProperty
        return stringProperty.value
    }

    public fun propertyStringValueOrNull(name: String): String? {
        val property = properties.firstOrNull { property -> property.name == name }
        val stringProperty = property?.value as? StringProperty
        return stringProperty?.value
    }

    public fun propertyBooleanValue(name: String): Boolean {
        val property = properties.first { property -> property.name == name }
        val stringProperty = property.value as BooleanProperty
        return stringProperty.value
    }

    public fun propertyBooleanValueOrNull(name: String): Boolean? {
        val property = properties.firstOrNull { property -> property.name == name }
        val stringProperty = property?.value as? BooleanProperty
        return stringProperty?.value
    }

    public data class Property(
        val name: String,
        val value: PropertyValue?,
    ) {
        internal fun isValid(): Boolean {
            if (name.isEmpty()) return false
            if (value == null) return true
            if (!value.isValid()) return false
            return true
        }
    }

    internal fun isValid(): Boolean {
        if (properties.any { property -> !property.isValid() }) return false

        val uniqueProperties = properties.distinctBy { property -> property.name }
        if (uniqueProperties.size != properties.size) return false

        return true
    }
}