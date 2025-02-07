package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters

import com.intellij.openapi.Disposable
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.Action
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SelectEnvironmentsDirectoryLocationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// TODO: Finish
internal class CreateEnvironmentPresenter(
    onSelectEnvironmentPathClicked: () -> Unit
) : Disposable {
    private val _state = MutableStateFlow<State>(
        value = SelectEnvironmentsDirectoryLocationState(
            path = "",
            verification = null,
            isNextButtonEnabled = false,
        )
    )
    val state: StateFlow<State> = _state

    fun onAction(action: Action) {
        when (action) {
            is Action.OnPreviousButtonClicked -> Unit
            is Action.OnNextButtonClicked -> Unit
            is Action.OnFinishButtonClicked -> Unit
            is Action.SelectEnvironmentsDirectoryLocationAction -> Unit
            is Action.SetupSchemaAction -> Unit
        }
    }

    override fun dispose() {

    }
}