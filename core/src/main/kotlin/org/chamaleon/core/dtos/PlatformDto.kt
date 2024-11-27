package org.chamaleon.core.dtos

import kotlinx.serialization.Serializable
import org.chamaleon.core.models.PlatformType
import org.chamaleon.core.models.PropertyValue
import org.chamaleon.core.serializers.PropertyDtoSerializer

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
