package io.github.gerardorodriguezdev.chamaleon.core.dtos

import kotlinx.serialization.Serializable

@Serializable
data class PropertiesDto(
    val selectedEnvironmentName: String,
)