package io.github.gerardorodriguezdev.chamaleon.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class PropertyType {
    @SerialName("String")
    STRING,

    @SerialName("Boolean")
    BOOLEAN,
}