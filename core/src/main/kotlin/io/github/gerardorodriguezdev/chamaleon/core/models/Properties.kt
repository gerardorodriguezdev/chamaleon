package io.github.gerardorodriguezdev.chamaleon.core.models

import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.NonEmptyString
import kotlinx.serialization.Serializable

@Serializable
public data class Properties(
    val selectedEnvironmentName: NonEmptyString? = null,
)