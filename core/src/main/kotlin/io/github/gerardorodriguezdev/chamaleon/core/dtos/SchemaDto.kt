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
    data class PropertyDefinitionDto(
        val name: String,
        val propertyType: PropertyType,
        val nullable: Boolean = false,
        @EncodeDefault
        val supportedPlatforms: Set<PlatformType> = emptySet(),
    ) {
        //TODO: Test
        fun isValid(supportedPlatforms: Set<PlatformType>): Boolean {
            if (name.isEmpty()) return false
            if (containsUnsupportedPlatforms(supportedPlatforms)) return false
            return true
        }

        private fun PropertyDefinitionDto.containsUnsupportedPlatforms(supportedPlatforms: Set<PlatformType>): Boolean =
            this.supportedPlatforms.isNotEmpty() && !supportedPlatforms.containsAll(this.supportedPlatforms)
    }

    //TODO: Test
    fun isValid(): ValidationResult {
        if (supportedPlatforms.isEmpty()) return ValidationResult.EMPTY_SUPPORTED_PLATFORMS
        if (propertyDefinitionDtos.isEmpty()) return ValidationResult.EMPTY_PROPERTY_DEFINITIONS
        if (propertyDefinitionDtos.any { propertyDefinition -> !propertyDefinition.isValid(supportedPlatforms) })
            return ValidationResult.INVALID_PROPERTY_DEFINITION

        val uniquePropertyDefinitions =
            propertyDefinitionDtos.distinctBy { propertyDefinition -> propertyDefinition.name }
        if (propertyDefinitionDtos.size != uniquePropertyDefinitions.size) return ValidationResult.DUPLICATED_PROPERTY_DEFINITION

        return ValidationResult.VALID
    }

    enum class ValidationResult {
        VALID,
        EMPTY_SUPPORTED_PLATFORMS,
        EMPTY_PROPERTY_DEFINITIONS,
        INVALID_PROPERTY_DEFINITION,
        DUPLICATED_PROPERTY_DEFINITION,
    }
}