package io.github.gerardorodriguezdev.chamaleon.core.models

import io.github.gerardorodriguezdev.chamaleon.core.safeModels.KeyProvider
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptySet
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString
import io.github.gerardorodriguezdev.chamaleon.core.serializers.SchemaSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable(with = SchemaSerializer::class)
public class Schema private constructor(
    @SerialName("supportedPlatforms")
    public val globalSupportedPlatformTypes: NonEmptySet<PlatformType>,
    public val propertyDefinitions: NonEmptyKeySetStore<String, PropertyDefinition>,
) {
    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    public data class PropertyDefinition(
        val name: NonEmptyString,
        val propertyType: PropertyType,
        val nullable: Boolean = false,
        @SerialName("supportedPlatforms")
        val supportedPlatformTypes: NonEmptySet<PlatformType>? = null,
    ) : KeyProvider<String> {
        override val key: String = name.value
    }

    public companion object {
        //TODO: Here full error
        public fun schemaOf(
            globalSupportedPlatformTypes: NonEmptySet<PlatformType>,
            propertyDefinitions: NonEmptyKeySetStore<String, PropertyDefinition>,
        ): Schema? {
            if (!arePropertyDefinitionsValid(globalSupportedPlatformTypes, propertyDefinitions)) return null
            return Schema(globalSupportedPlatformTypes, propertyDefinitions)
        }

        private fun arePropertyDefinitionsValid(
            globalSupportedPlatformTypes: NonEmptySet<PlatformType>,
            propertyDefinitions: NonEmptyKeySetStore<String, PropertyDefinition>,
        ): Boolean =
            propertyDefinitions.values.all { propertyDefinition ->
                propertyDefinition.arePlatformsSupported(globalSupportedPlatformTypes)
            }

        private fun PropertyDefinition.arePlatformsSupported(
            globalSupportedPlatforms: NonEmptySet<PlatformType>
        ): Boolean = supportedPlatformTypes == null || globalSupportedPlatforms.containsAll(supportedPlatformTypes)
    }
}