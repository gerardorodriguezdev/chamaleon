package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter

import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.Action.SetupEnvironmentAction

class SetupEnvironmentPresenter {
    fun onAction(action: SetupEnvironmentAction) {
        when (action) {
            is SetupEnvironmentAction.OnSelectEnvironmentPathClicked -> Unit
            is SetupEnvironmentAction.OnEnvironmentNameChanged -> Unit
        }
    }
}