package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter

import com.intellij.openapi.Disposable
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.base.StateHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.CoroutineContext

internal class CreateEnvironmentPresenter(
    uiContext: CoroutineContext,
    setupEnvironmentPresenterProvider: (
        stateHolder: StateHolder<CreateEnvironmentState>,
        uiScope: CoroutineScope,
    ) -> SetupEnvironmentPresenter
) : Disposable, StateHolder<CreateEnvironmentState> {
    private val mutableStateFlow = MutableStateFlow<CreateEnvironmentState>(CreateEnvironmentState())
    val stateFlow: StateFlow<CreateEnvironmentState> = mutableStateFlow

    override val state: CreateEnvironmentState get() = mutableStateFlow.value

    private val uiScope = CoroutineScope(uiContext)

    private val setupEnvironmentPresenter = setupEnvironmentPresenterProvider(this, uiScope)

    fun onAction(action: CreateEnvironmentAction) {
        when (action) {
            is CreateEnvironmentAction.SetupEnvironmentAction -> setupEnvironmentPresenter.onAction(action)
            is CreateEnvironmentAction.DialogAction -> Unit
            is CreateEnvironmentAction.SetupPropertiesAction -> Unit
            is CreateEnvironmentAction.SetupSchemaAction -> Unit
        }
    }

    override fun updateState(block: (CreateEnvironmentState) -> CreateEnvironmentState) {
        block.invoke(state)
    }

    override fun dispose() {
        uiScope.cancel()
    }
}