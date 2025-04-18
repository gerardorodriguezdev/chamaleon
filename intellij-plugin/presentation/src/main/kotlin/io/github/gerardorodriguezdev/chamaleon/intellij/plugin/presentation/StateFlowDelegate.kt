package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class StateFlowDelegate<T>(private val stateFlow: MutableStateFlow<T>) : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = stateFlow.value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        stateFlow.value = value
    }
}

internal fun <T> MutableStateFlow<T>.asDelegate() = StateFlowDelegate(this)
