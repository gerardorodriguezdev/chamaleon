package io.github.gerardorodriguezdev.chamaleon.core.models

import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString
import kotlinx.serialization.Serializable

@Serializable
public data class Properties(
    val selectedEnvironmentName: NonEmptyString? = null,
)