package org.chamaleon.core.dtos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.chamaleon.core.models.PlatformType
import org.chamaleon.core.models.PropertyType

@Serializable
internal data class SchemaDto(
    val supportedPlatforms: Set<PlatformType>,
    @SerialName("propertyDefinitions")
    val propertyDefinitionDtos: Set<PropertyDefinitionDto>,
) {
    @Serializable
    internal data class PropertyDefinitionDto(
        val name: String,
        val propertyType: PropertyType,
        val nullable: Boolean = false,
    )
}
