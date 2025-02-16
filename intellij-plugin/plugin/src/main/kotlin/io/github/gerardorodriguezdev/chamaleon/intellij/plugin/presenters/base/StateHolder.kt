package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.base

internal interface StateHolder<T> {
    val state: T

    fun updateState(block: (currentState: T) -> T)
}