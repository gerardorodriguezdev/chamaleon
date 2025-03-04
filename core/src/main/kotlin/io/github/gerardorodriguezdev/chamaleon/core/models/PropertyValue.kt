package io.github.gerardorodriguezdev.chamaleon.core.models

import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString

public sealed class PropertyValue {
    public data class StringProperty(val value: NonEmptyString) : PropertyValue() {
        override fun toString(): String = value.value
    }

    public data class BooleanProperty(val value: Boolean) : PropertyValue() {
        override fun toString(): String = value.toString()
    }
}