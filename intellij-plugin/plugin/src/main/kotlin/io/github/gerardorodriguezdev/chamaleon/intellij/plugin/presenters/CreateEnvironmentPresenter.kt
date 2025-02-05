package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters

import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.Action
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SelectEnvironmentsDirectoryLocationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// TODO: Finish
internal class CreateEnvironmentPresenter(onSelectEnvironmentPathClicked: () -> Unit) {
    private val _state = MutableStateFlow<State>(
        value = SelectEnvironmentsDirectoryLocationState(path = "", verification = null)
    )
    val state: StateFlow<State> = _state

    fun onAction(action: Action) {
        when (action) {
            is Action.OnSelectEnvironmentPathClicked -> Unit
            is Action.OnSupportedPlatformTypeChecked -> Unit
        }
    }
}