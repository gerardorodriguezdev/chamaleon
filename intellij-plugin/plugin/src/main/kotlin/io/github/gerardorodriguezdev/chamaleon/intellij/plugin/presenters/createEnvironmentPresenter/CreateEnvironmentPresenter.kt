package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter

import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.base.StateHolder
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.delegates.SetupEnvironmentPresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class CreateEnvironmentPresenter(
    uiScope: CoroutineScope,
    setupEnvironmentPresenterProvider: (
        stateHolder: StateHolder<CreateEnvironmentState>,
        uiScope: CoroutineScope,
    ) -> SetupEnvironmentPresenter
) : StateHolder<CreateEnvironmentState> {
    private val mutableStateFlow = MutableStateFlow<CreateEnvironmentState>(CreateEnvironmentState())
    val stateFlow: StateFlow<CreateEnvironmentState> = mutableStateFlow

    override val state: CreateEnvironmentState get() = mutableStateFlow.value

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
        mutableStateFlow.value = block.invoke(state)
    }
}