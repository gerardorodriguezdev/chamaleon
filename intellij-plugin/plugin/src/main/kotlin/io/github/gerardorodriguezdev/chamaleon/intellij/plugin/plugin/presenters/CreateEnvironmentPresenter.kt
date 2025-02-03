package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.plugin.presenters

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

    sealed interface State {
        data class SelectEnvironmentPath(val name: String) : State
        data class SelectSchema(val name: String, val schema: String) : State
    }

    sealed interface Action {
        data object OnSelectEnvironmentPathClicked : Action
    }
}