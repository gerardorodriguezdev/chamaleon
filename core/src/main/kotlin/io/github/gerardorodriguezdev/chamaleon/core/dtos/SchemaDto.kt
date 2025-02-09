package io.github.gerardorodriguezdev.chamaleon.core.dtos

import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyType
import io.github.gerardorodriguezdev.chamaleon.core.serializers.NonEmptyStringSerializer
import io.github.gerardorodriguezdev.chamaleon.core.serializers.SchemaDtoSerializer
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable(with = SchemaDtoSerializer::class)
internal data class SchemaDto(
    val supportedPlatforms: Set<PlatformType>,
    @SerialName("propertyDefinitions")
    val propertyDefinitionsDtos: Set<PropertyDefinitionDto>,
) {
    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    data class PropertyDefinitionDto(
        @Serializable(with = NonEmptyStringSerializer::class)
        val name: String,
        val propertyType: PropertyType,
        val nullable: Boolean = false,
        @EncodeDefault
        val supportedPlatforms: Set<PlatformType> = emptySet(),
    )
}