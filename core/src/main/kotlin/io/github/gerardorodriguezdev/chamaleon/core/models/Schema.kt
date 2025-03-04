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
        public fun schemaOf(
            globalSupportedPlatformTypes: NonEmptySet<PlatformType>,
            propertyDefinitions: NonEmptyKeySetStore<String, PropertyDefinition>,
        ): Schema? {

            //TODO: Sep?
            propertyDefinitions.values.forEach { propertyDefinition ->
                if (!propertyDefinition.arePlatformsSupported(globalSupportedPlatformTypes)) return null
            }

            return Schema(globalSupportedPlatformTypes, propertyDefinitions)
        }

        private fun PropertyDefinition.arePlatformsSupported(
            globalSupportedPlatforms: NonEmptySet<PlatformType>
        ): Boolean = supportedPlatformTypes == null || globalSupportedPlatforms.containsAll(supportedPlatformTypes)
    }
}