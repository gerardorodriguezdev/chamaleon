package io.github.gerardorodriguezdev.chamaleon.core.dtos

import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyValue
import io.github.gerardorodriguezdev.chamaleon.core.serializers.PropertyDtoSerializer
import kotlinx.serialization.Serializable

@Serializable
internal data class PlatformDto(
    val platformType: PlatformType,
    val properties: Set<PropertyDto>,
) {
    @Serializable(with = PropertyDtoSerializer::class)
    data class PropertyDto(
        val name: String,
        val value: PropertyValue?,
    ) {
        fun isValid(): Boolean {
            if (name.isEmpty()) return false
            if (value == null) return true
            if (!value.isValid()) return false
            return true
        }
    }

    fun isValid(): ValidationResult {
        if (properties.isEmpty()) return ValidationResult.EMPTY_PROPERTIES

        if (properties.any { property -> !property.isValid() }) return ValidationResult.INVALID_PROPERTY

        return ValidationResult.VALID
    }

    enum class ValidationResult {
        VALID,
        EMPTY_PROPERTIES,
        INVALID_PROPERTY,
    }
}