package io.github.gerardorodriguezdev.chamaleon.core.models

import kotlinx.serialization.SerialName

enum class PropertyType {
    @SerialName("String")
    STRING,

    @SerialName("Boolean")
    BOOLEAN,
}