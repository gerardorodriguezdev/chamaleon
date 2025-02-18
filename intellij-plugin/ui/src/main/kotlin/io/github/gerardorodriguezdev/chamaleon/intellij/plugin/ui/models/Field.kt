package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.models

data class Field<T>(
    val value: T,
    val verification: Verification? = null,
) {
    sealed interface Verification {
        data object Valid : Verification
        data class Invalid(val reason: String) : Verification
        data object Loading : Verification
    }
}