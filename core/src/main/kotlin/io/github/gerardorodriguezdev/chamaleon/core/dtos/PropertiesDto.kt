package io.github.gerardorodriguezdev.chamaleon.core.dtos

import kotlinx.serialization.Serializable

@Serializable
internal data class PropertiesDto(
    val selectedEnvironmentName: String?,
)