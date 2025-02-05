package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters

import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.Action
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// TODO: Finish + Test
internal class CreateEnvironmentPresenter(
    onSelectEnvironmentPathClicked: () -> Unit,
) {
    private val _state = MutableStateFlow<State>(value = State.SelectEnvironmentPath(name = ""))
    val state: StateFlow<State> = _state

    fun onAction(action: Action) {
        when (action) {
            is Action.OnSelectEnvironmentPathClicked -> Unit
        }
    }
}