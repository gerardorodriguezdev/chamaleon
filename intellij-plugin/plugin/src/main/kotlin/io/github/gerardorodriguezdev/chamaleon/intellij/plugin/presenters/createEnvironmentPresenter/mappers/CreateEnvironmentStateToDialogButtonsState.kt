package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.mappers

import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.BaseDialog.DialogButtonsState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState.EnvironmentsDirectoryProcessResult
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState.Step

internal fun CreateEnvironmentState.toDialogButtonsState(): DialogButtonsState =
    DialogButtonsState(
        isPreviousButtonEnabled = isPreviousButtonEnabled(),
        isNextButtonEnabled = isNextButtonEnabled(),
        isFinishButtonEnabled = isFinishButtonEnabled(),
    )

private fun CreateEnvironmentState.isPreviousButtonEnabled(): Boolean =
    when (step) {
        Step.SETUP_ENVIRONMENT -> false
        Step.SETUP_SCHEMA -> true
    }

private fun CreateEnvironmentState.isNextButtonEnabled(): Boolean =
    when (step) {
        Step.SETUP_ENVIRONMENT ->
            environmentsDirectoryProcessResult.isValid() && toEnvironmentNameVerification().isValid()

        Step.SETUP_SCHEMA -> schema.environmentsValidationResults(environments).areEnvironmentsValid()
    }

private fun CreateEnvironmentState.isFinishButtonEnabled(): Boolean =
    when (step) {
        Step.SETUP_ENVIRONMENT -> false
        Step.SETUP_SCHEMA -> false
    }

private fun EnvironmentsDirectoryProcessResult.isValid(): Boolean = this is EnvironmentsDirectoryProcessResult.Success

private fun List<Schema.EnvironmentsValidationResult>.areEnvironmentsValid(): Boolean =
    !any { environmentValidationResult ->
        environmentValidationResult is Schema.EnvironmentsValidationResult.Failure
    }