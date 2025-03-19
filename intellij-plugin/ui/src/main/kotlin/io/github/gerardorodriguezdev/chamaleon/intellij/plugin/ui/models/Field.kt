package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.models

data class Field<T>(
    val value: T,
    val verification: Verification?,
) {
    sealed interface Verification {
        fun asInvalid(): Invalid? = (this as? Invalid)

        data object Valid : Verification
        data class Invalid(val reason: String) : Verification
        data object Loading : Verification
    }

    companion object {
        fun emptyStringField(): Field<String> = Field(value = "", verification = null)
    }
}