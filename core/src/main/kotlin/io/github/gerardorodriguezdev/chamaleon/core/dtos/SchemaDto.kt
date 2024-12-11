package io.github.gerardorodriguezdev.chamaleon.core.dtos

import io.github.gerardorodriguezdev.chamaleon.core.models.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
