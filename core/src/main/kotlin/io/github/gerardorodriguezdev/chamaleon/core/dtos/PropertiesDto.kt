package io.github.gerardorodriguezdev.chamaleon.core.dtos

import io.github.gerardorodriguezdev.chamaleon.core.serializers.NonEmptyNullableStringSerializer
import kotlinx.serialization.Serializable

@Serializable
internal data class PropertiesDto(
    @Serializable(with = NonEmptyNullableStringSerializer::class)
    val selectedEnvironmentName: String?,
)