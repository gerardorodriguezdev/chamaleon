package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter

import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.base.StateHolder
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState.Step
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.delegates.SetupEnvironmentPresenter
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.delegates.SetupSchemaPresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class CreateEnvironmentPresenter(
    uiScope: CoroutineScope,
    setupEnvironmentPresenterProvider: (
        stateHolder: StateHolder<CreateEnvironmentState>,
        uiScope: CoroutineScope,
    ) -> SetupEnvironmentPresenter,
    setupSchemaPresenterProvider: (stateHolder: StateHolder<CreateEnvironmentState>) -> SetupSchemaPresenter,
) : StateHolder<CreateEnvironmentState> {
    private val mutableStateFlow = MutableStateFlow<CreateEnvironmentState>(CreateEnvironmentState())
    val stateFlow: StateFlow<CreateEnvironmentState> = mutableStateFlow

    override val state: CreateEnvironmentState get() = mutableStateFlow.value

    private val setupEnvironmentPresenter = setupEnvironmentPresenterProvider(this, uiScope)
    private val setupSchemaPresenter = setupSchemaPresenterProvider(this)

    fun onAction(action: CreateEnvironmentAction) {
        when (action) {
            is CreateEnvironmentAction.SetupEnvironmentAction -> setupEnvironmentPresenter.onAction(action)
            is CreateEnvironmentAction.SetupSchemaAction -> setupSchemaPresenter.onAction(action)
            is CreateEnvironmentAction.SetupPropertiesAction -> Unit
            is CreateEnvironmentAction.DialogAction -> action.handle()
        }
    }

    //TODO: Move external presenter
    private fun CreateEnvironmentAction.DialogAction.handle() {
        when (this) {
            is CreateEnvironmentAction.DialogAction.OnPreviousButtonClicked -> Unit

            is CreateEnvironmentAction.DialogAction.OnNextButtonClicked ->
                when (state.step) {
                    Step.SETUP_ENVIRONMENT -> updateSetupEnvironmentNextStep()
                    Step.SETUP_SCHEMA -> Unit
                }

            is CreateEnvironmentAction.DialogAction.OnFinishButtonClicked -> Unit
        }
    }

    override fun updateState(block: (CreateEnvironmentState) -> CreateEnvironmentState) {
        mutableStateFlow.value = block.invoke(state)
    }

    private fun updateSetupEnvironmentNextStep() {
        updateState { currentState -> currentState.copy(step = Step.SETUP_SCHEMA) }
    }
}