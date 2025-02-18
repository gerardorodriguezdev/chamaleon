package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.delegates

import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.base.StateHolder
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentAction.SetupSchemaAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentAction.SetupSchemaAction.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState

interface SetupSchemaPresenter {
    fun onAction(action: SetupSchemaAction)
}

internal class DefaultSetupSchemaPresenter(
    private val stateHolder: StateHolder<CreateEnvironmentState>,
) : SetupSchemaPresenter {

    override fun onAction(action: SetupSchemaAction) {
        when (action) {
            is OnSupportedPlatformChanged -> action.handle()
            is OnAddPropertyDefinitionClicked -> Unit
            is OnNullableChanged -> Unit
            is OnPropertyDefinitionSupportedPlatformChanged -> Unit
            is OnPropertyNameChanged -> Unit
            is OnPropertyTypeChanged -> Unit
        }
    }

    private fun OnSupportedPlatformChanged.handle() {
        stateHolder.updateState { currentState ->
            val currentSchema = currentState.schema
            val currentSupportedPlatforms = currentSchema.supportedPlatforms
            val newSupportedPlatforms = currentSupportedPlatforms + newPlatformType
            currentState.copy(
                schema = currentSchema.copy(
                    newSupportedPlatforms,
                ),
            )
        }
    }
}