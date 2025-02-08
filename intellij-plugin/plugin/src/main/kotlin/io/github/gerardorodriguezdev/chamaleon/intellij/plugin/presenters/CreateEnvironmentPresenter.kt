package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters

import com.intellij.openapi.Disposable
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.Action
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.Action.SetupEnvironmentAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.LoadingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.CoroutineContext

internal class CreateEnvironmentPresenter(
    uiDispatcher: CoroutineContext,
    ioDispatcher: CoroutineContext,
    private val onSelectEnvironmentPathClicked: () -> String?,
) : Disposable {
    //TODO: Title for setup schema depending if schema existing or not
    //TODO: Set root automatically if nothing selected
    //TODO: Execute externally notification of progress
    //TODO: Rev if valids or nots
    //TODO: Move logic if possible to processor
    private val _state = MutableStateFlow<State>(value = LoadingState())
    val state: StateFlow<State> = _state

    private val ioScope = CoroutineScope(ioDispatcher)

    fun onAction(action: Action) {
        when (action) {
            is Action.ExternalAction -> Unit
            is SetupEnvironmentAction -> action.handle()
            is Action.SetupSchemaAction -> Unit
            is Action.SetupPropertiesAction -> Unit
        }
    }

    private fun SetupEnvironmentAction.handle() {
        when (this) {
            is SetupEnvironmentAction.OnSelectEnvironmentPathClicked -> {
                val selectedEnvironmentPath = onSelectEnvironmentPathClicked()
                selectedEnvironmentPath?.let { path ->
                    //TODO: Update path selected here
                }
            }

            is SetupEnvironmentAction.OnEnvironmentNameChanged -> Unit //TODO: Finish
        }
    }

    override fun dispose() {
        ioScope.cancel()
    }
}