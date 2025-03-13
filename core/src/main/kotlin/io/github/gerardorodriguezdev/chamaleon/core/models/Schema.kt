package io.github.gerardorodriguezdev.chamaleon.core.models

import arrow.core.Either
import arrow.core.raise.either
import io.github.gerardorodriguezdev.chamaleon.core.results.SchemaValidationResult
import io.github.gerardorodriguezdev.chamaleon.core.results.SchemaValidationResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.results.SchemaValidationResult.Success
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.*
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
        ): SchemaValidationResult =
            either {
                propertyDefinitions.values.forEach { propertyDefinition ->
                    propertyDefinition.arePlatformsSupported(globalSupportedPlatformTypes).bind()
                }

                Success(Schema(globalSupportedPlatformTypes, propertyDefinitions))
            }.fold(
                ifLeft = { it },
                ifRight = { it },
            )

        private fun PropertyDefinition.arePlatformsSupported(
            globalSupportedPlatformTypes: NonEmptySet<PlatformType>
        ): Either<Failure, InternalSuccess> =
            either {
                when {
                    supportedPlatformTypes == null -> InternalSuccess
                    globalSupportedPlatformTypes.containsAll(supportedPlatformTypes) -> InternalSuccess
                    else -> raise(
                        Failure.UnsupportedPlatformTypesOnPropertyDefinition(
                            globalSupportedPlatformTypes = globalSupportedPlatformTypes,
                            propertyDefinition = this@arePlatformsSupported,
                            unsupportedPlatformTypes = supportedPlatformTypes - globalSupportedPlatformTypes,
                        )
                    )
                }
            }
    }
}