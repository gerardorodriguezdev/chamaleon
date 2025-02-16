package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.base

import kotlinx.coroutines.flow.MutableStateFlow

internal interface StateHolder<T> {
    val mutableState: MutableStateFlow<T>
}