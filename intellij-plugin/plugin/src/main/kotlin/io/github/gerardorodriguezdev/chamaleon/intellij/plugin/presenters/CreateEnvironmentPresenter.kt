package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters

import com.intellij.openapi.Disposable
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.Action
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.LoadingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.CoroutineContext

// TODO: Finish
internal class CreateEnvironmentPresenter(
    uiDispatcher: CoroutineContext,
    ioDispatcher: CoroutineContext,
    onSelectEnvironmentPathClicked: () -> Unit
) : Disposable {
    //TODO: Title for setup schema depending if schema existing or not
    //TODO: Set root automatically if nothing selected
    //TODO: Execute externally notification of progress
    //TODO: Rev if valids or nots
    //TODO: Move logic if possible to processor
    private val _state = MutableStateFlow<State>(
        value = LoadingState()
    )
    val state: StateFlow<State> = _state

    fun onAction(action: Action) {
        when (action) {
            is Action.ExternalAction -> Unit
            is Action.SetupEnvironmentAction -> Unit
            is Action.SetupSchemaAction -> Unit
            is Action.SetupPropertiesAction -> Unit
        }
    }

    override fun dispose() {

    }
}