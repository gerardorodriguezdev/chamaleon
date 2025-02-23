package io.github.gerardorodriguezdev.chamaleon.core.entities

public sealed class PropertyValue {
    internal abstract fun isValid(): Boolean

    public data class StringProperty(val value: String) : PropertyValue() {
        override fun isValid(): Boolean = value.isNotEmpty()
        override fun toString(): String = value
    }

    public data class BooleanProperty(val value: Boolean) : PropertyValue() {
        override fun isValid(): Boolean = true
        override fun toString(): String = value.toString()
    }
}