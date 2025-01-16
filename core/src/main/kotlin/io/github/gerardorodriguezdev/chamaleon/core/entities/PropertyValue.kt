package io.github.gerardorodriguezdev.chamaleon.core.entities

public sealed interface PropertyValue {
    public data class StringProperty(val value: String) : PropertyValue
    public data class BooleanProperty(val value: Boolean) : PropertyValue
}