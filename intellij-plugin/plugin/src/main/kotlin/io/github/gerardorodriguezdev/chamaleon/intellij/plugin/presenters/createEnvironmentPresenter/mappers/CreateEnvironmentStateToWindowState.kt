package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.mappers

import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState.EnvironmentsDirectoryProcessResult
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState.Step
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.models.Field
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowState.SetupEnvironmentState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowState.SetupSchemaState
import kotlinx.collections.immutable.persistentListOf

internal fun CreateEnvironmentState.toWindowState(): CreateEnvironmentWindowState =
    when (step) {
        Step.SETUP_ENVIRONMENT -> {
            SetupEnvironmentState(
                environmentsDirectoryPathField = Field(
                    value = environmentsDirectoryPath,
                    verification = environmentsDirectoryProcessResult.toVerification(),
                ),
                environmentNameField = Field(
                    value = environmentName,
                    verification = toEnvironmentNameVerification().toVerification()
                )
            )
        }

        //TODO: Update
        Step.SETUP_SCHEMA -> {
            SetupSchemaState(
                title = "Update",
                supportedPlatforms = persistentListOf(),
                propertyDefinitions = persistentListOf(),
            )
        }
    }

private fun EnvironmentsDirectoryProcessResult.toVerification(): Field.Verification =
    when (this) {
        is EnvironmentsDirectoryProcessResult.Success -> Field.Verification.Valid
        is EnvironmentsDirectoryProcessResult.Loading -> Field.Verification.Loading
        is EnvironmentsDirectoryProcessResult.Failure -> when (this) {
            is EnvironmentsDirectoryProcessResult.Failure.InvalidEnvironments -> Field.Verification.Invalid("Invalid environments") //TODO: Fix
            is EnvironmentsDirectoryProcessResult.Failure.FileIsNotDirectory -> Field.Verification.Invalid("File not directory") //TODO: Fix
            is EnvironmentsDirectoryProcessResult.Failure.EnvironmentsDirectoryNotFound -> Field.Verification.Valid
            is EnvironmentsDirectoryProcessResult.Failure.SchemaFileNotFound -> Field.Verification.Valid
        }
    }

private fun EnvironmentNameVerification.toVerification(): Field.Verification =
    when (this) {
        EnvironmentNameVerification.VALID -> Field.Verification.Valid
        EnvironmentNameVerification.IS_EMPTY -> Field.Verification.Invalid("Environment name empty") //TODO: Fix
        EnvironmentNameVerification.IS_DUPLICATED -> Field.Verification.Invalid("Environment name duplicated") //TODO: Fix
    }