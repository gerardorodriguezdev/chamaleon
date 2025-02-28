package io.github.gerardorodriguezdev.chamaleon.core.models

import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.BooleanProperty
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.StringProperty
import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.KeyProvider
import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.NonEmptyKeyStore
import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.NonEmptyString
import io.github.gerardorodriguezdev.chamaleon.core.serializers.PropertySerializer
import kotlinx.serialization.Serializable

@Serializable
public data class Platform(
    val platformType: PlatformType,
    val properties: NonEmptyKeyStore<String, Property>,
) : KeyProvider<PlatformType> {
    override val key: PlatformType = platformType

    public fun propertyStringValue(name: String): String {
        val property = properties.getValue(name)
        val stringProperty = property.value as StringProperty
        return stringProperty.value.value
    }

    public fun propertyStringValueOrNull(name: String): String? {
        val property = properties[name]
        val stringProperty = property?.value as? StringProperty
        return stringProperty?.value?.value
    }

    public fun propertyBooleanValue(name: String): Boolean {
        val property = properties.getValue(name)
        val stringProperty = property.value as BooleanProperty
        return stringProperty.value
    }

    public fun propertyBooleanValueOrNull(name: String): Boolean? {
        val property = properties[name]
        val stringProperty = property?.value as? BooleanProperty
        return stringProperty?.value
    }

    @Serializable(with = PropertySerializer::class)
    public data class Property(
        val name: NonEmptyString,
        val value: PropertyValue?,
    ) : KeyProvider<String> {
        override val key: String = name.value
    }
}