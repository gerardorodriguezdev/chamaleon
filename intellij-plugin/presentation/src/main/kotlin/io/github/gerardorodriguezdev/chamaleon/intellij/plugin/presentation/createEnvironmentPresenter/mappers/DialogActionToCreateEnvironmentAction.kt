package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createEnvironmentPresenter.mappers

import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createEnvironmentPresenter.CreateEnvironmentAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createEnvironmentPresenter.CreateEnvironmentAction.DialogAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createEnvironmentPresenter.CreateEnvironmentAction.DialogAction.*

internal fun DialogAction.toCreateEnvironmentAction(): CreateEnvironmentAction =
    when (this) {
        is OnPreviousButtonClicked -> OnPreviousButtonClicked
        is OnNextButtonClicked -> OnNextButtonClicked
        is OnFinishButtonClicked -> OnFinishButtonClicked
    }