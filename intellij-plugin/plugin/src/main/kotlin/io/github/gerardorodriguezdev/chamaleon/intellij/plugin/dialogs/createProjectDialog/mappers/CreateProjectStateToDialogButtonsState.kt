package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.createProjectDialog.mappers

import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.BaseDialog.DialogButtonsState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.CreateProjectState

internal fun CreateProjectState.toDialogButtonsState(): DialogButtonsState =
    DialogButtonsState(
        isPreviousButtonEnabled = toPrevious() != null,
        isNextButtonEnabled = toNext() != null,
        isFinishButtonEnabled = toFinish() != null,
    )