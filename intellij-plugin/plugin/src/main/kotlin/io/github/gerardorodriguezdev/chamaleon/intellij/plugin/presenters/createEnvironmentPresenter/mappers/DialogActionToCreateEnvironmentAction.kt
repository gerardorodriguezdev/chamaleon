package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.mappers

import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.BaseDialog.DialogAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.BaseDialog.DialogAction.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentAction

internal fun DialogAction.toCreateEnvironmentAction(): CreateEnvironmentAction =
    when (this) {
        is OnPreviousButtonClicked -> CreateEnvironmentAction.DialogAction.OnPreviousButtonClicked
        is OnNextButtonClicked -> CreateEnvironmentAction.DialogAction.OnNextButtonClicked
        is OnFinishButtonClicked -> CreateEnvironmentAction.DialogAction.OnFinishButtonClicked
    }