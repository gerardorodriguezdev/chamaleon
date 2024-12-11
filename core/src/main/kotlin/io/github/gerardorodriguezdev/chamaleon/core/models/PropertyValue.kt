package io.github.gerardorodriguezdev.chamaleon.core.models

sealed interface PropertyValue {
    data class StringProperty(val value: String) : PropertyValue
    data class BooleanProperty(val value: Boolean) : PropertyValue
}