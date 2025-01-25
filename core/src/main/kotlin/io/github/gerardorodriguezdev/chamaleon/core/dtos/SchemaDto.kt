package io.github.gerardorodriguezdev.chamaleon.core.dtos

import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyType
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SchemaDto(
    val supportedPlatforms: Set<PlatformType>,
    @SerialName("propertyDefinitions")
    val propertyDefinitionDtos: Set<PropertyDefinitionDto>,
) {
    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    internal data class PropertyDefinitionDto(
        val name: String,
        val propertyType: PropertyType,
        val nullable: Boolean = false,
        @EncodeDefault
        val supportedPlatforms: Set<PlatformType> = emptySet(),
    )
}