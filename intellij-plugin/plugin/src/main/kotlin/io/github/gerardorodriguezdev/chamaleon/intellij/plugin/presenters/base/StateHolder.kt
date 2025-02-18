package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.base

//TODO: Remove
internal interface StateHolder<T> {
    val state: T

    //TODO: Scoping
    fun updateState(block: (currentState: T) -> T)
}