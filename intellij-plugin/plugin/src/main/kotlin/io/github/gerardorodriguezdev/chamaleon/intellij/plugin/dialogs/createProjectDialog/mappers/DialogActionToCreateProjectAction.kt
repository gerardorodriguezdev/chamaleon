package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.createProjectDialog.mappers

import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.BaseDialog
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.CreateProjectAction

internal fun BaseDialog.DialogAction.toCreateProjectAction(): CreateProjectAction.NavigationAction =
    when (this) {
        is BaseDialog.DialogAction.OnPreviousButtonClicked -> CreateProjectAction.NavigationAction.OnPrevious
        is BaseDialog.DialogAction.OnNextButtonClicked -> CreateProjectAction.NavigationAction.OnNext
        is BaseDialog.DialogAction.OnFinishButtonClicked -> CreateProjectAction.NavigationAction.OnFinish
    }