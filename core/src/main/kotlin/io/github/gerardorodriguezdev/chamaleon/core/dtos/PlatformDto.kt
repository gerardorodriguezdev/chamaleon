package io.github.gerardorodriguezdev.chamaleon.core.dtos

import io.github.gerardorodriguezdev.chamaleon.core.models.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue
import io.github.gerardorodriguezdev.chamaleon.core.serializers.PropertyDtoSerializer
import kotlinx.serialization.Serializable

@Serializable
internal data class PlatformDto(
    val platformType: PlatformType,
    val properties: Set<PropertyDto>,
) {
    @Serializable(with = PropertyDtoSerializer::class)
    internal data class PropertyDto(
        val name: String,
        val value: PropertyValue?,
    )
}
