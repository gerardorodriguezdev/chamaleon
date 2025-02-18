package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.mappers

import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.BaseDialog.DialogAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.BaseDialog.DialogAction.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState.EnvironmentsDirectoryProcessResult
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState.Step
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.models.Field
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowAction.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowAction.SetupEnvironmentAction.OnEnvironmentNameChanged
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowAction.SetupEnvironmentAction.OnSelectEnvironmentPathClicked
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowAction.SetupSchemaAction.*
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
                    verification = environmentName.toEnvironmentNameVerification(environments).toVerification()
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

private fun String.toEnvironmentNameVerification(environments: Set<Environment>): EnvironmentNameVerification =
    when {
        isEmpty() -> EnvironmentNameVerification.IS_EMPTY
        isDuplicated(environments) -> EnvironmentNameVerification.IS_DUPLICATED
        else -> EnvironmentNameVerification.VALID
    }

private fun String.isDuplicated(environments: Set<Environment>): Boolean =
    environments.any { environment -> environment.name == this }

private fun EnvironmentNameVerification.toVerification(): Field.Verification =
    when (this) {
        EnvironmentNameVerification.VALID -> Field.Verification.Valid
        EnvironmentNameVerification.IS_EMPTY -> Field.Verification.Invalid("Environment name empty") //TODO: Fix
        EnvironmentNameVerification.IS_DUPLICATED -> Field.Verification.Invalid("Environment name duplicated") //TODO: Fix
    }

private enum class EnvironmentNameVerification {
    VALID,
    IS_EMPTY,
    IS_DUPLICATED;

    fun isValid(): Boolean = this == VALID
}

fun CreateEnvironmentWindowAction.toCreateEnvironmentAction(): CreateEnvironmentAction =
    when (this) {
        is SetupEnvironmentAction -> toSetupEnvironmentAction()
        is SetupSchemaAction -> toSetupSchemaAction()
        is SetupPropertiesAction -> toSetupPropertiesAction()
    }

private fun SetupEnvironmentAction.toSetupEnvironmentAction(): CreateEnvironmentAction.SetupEnvironmentAction =
    when (this) {
        is OnSelectEnvironmentPathClicked ->
            CreateEnvironmentAction.SetupEnvironmentAction.OnSelectEnvironmentPathClicked

        is OnEnvironmentNameChanged ->
            CreateEnvironmentAction.SetupEnvironmentAction.OnEnvironmentNameChanged(newName = newName)
    }

private fun SetupSchemaAction.toSetupSchemaAction(): CreateEnvironmentAction.SetupSchemaAction =
    when (this) {
        is OnSupportedPlatformChanged ->
            CreateEnvironmentAction.SetupSchemaAction.OnSupportedPlatformChanged(newPlatformType = newPlatformType)

        is OnAddPropertyDefinitionClicked -> CreateEnvironmentAction.SetupSchemaAction.OnAddPropertyDefinitionClicked

        is OnPropertyNameChanged ->
            CreateEnvironmentAction.SetupSchemaAction.OnPropertyNameChanged(index = index, newName = newName)

        is OnPropertyTypeChanged -> CreateEnvironmentAction.SetupSchemaAction.OnPropertyTypeChanged(
            index = index,
            newPropertyType = newPropertyType
        )

        is OnNullableChanged ->
            CreateEnvironmentAction.SetupSchemaAction.OnNullableChanged(index = index, newValue = newValue)

        is OnPropertyDefinitionSupportedPlatformChanged ->
            CreateEnvironmentAction.SetupSchemaAction.OnPropertyDefinitionSupportedPlatformChanged(
                index = index,
                newPlatformType = newPlatformType
            )
    }

private fun SetupPropertiesAction.toSetupPropertiesAction(): CreateEnvironmentAction.SetupPropertiesAction =
    when (this) {
        is SetupPropertiesAction.OnPropertyNameChanged ->
            CreateEnvironmentAction.SetupPropertiesAction.OnPropertyNameChanged(
                index = index,
                newName = newName
            )

        is SetupPropertiesAction.OnPropertyValueChanged ->
            CreateEnvironmentAction.SetupPropertiesAction.OnPropertyValueChanged(
                index = index,
                newValue = newValue
            )
    }

internal fun DialogAction.toCreateEnvironmentAction(): CreateEnvironmentAction =
    when (this) {
        is OnPreviousButtonClicked -> CreateEnvironmentAction.DialogAction.OnPreviousButtonClicked
        is OnNextButtonClicked -> CreateEnvironmentAction.DialogAction.OnNextButtonClicked
        is OnFinishButtonClicked -> CreateEnvironmentAction.DialogAction.OnFinishButtonClicked
    }

internal fun CreateEnvironmentState.isPreviousButtonEnabled(): Boolean =
    when (step) {
        Step.SETUP_ENVIRONMENT -> false
        Step.SETUP_SCHEMA -> true
    }

internal fun CreateEnvironmentState.isNextButtonEnabled(): Boolean =
    when (step) {
        Step.SETUP_ENVIRONMENT ->
            environmentsDirectoryProcessResult.isValid() &&
                    environmentName.toEnvironmentNameVerification(environments).isValid()

        Step.SETUP_SCHEMA -> schema.environmentsValidationResults(environments).areEnvironmentsValid()
    }

internal fun CreateEnvironmentState.isFinishButtonEnabled(): Boolean =
    when (step) {
        Step.SETUP_ENVIRONMENT -> false
        Step.SETUP_SCHEMA -> false
    }

private fun EnvironmentsDirectoryProcessResult.isValid(): Boolean = this is EnvironmentsDirectoryProcessResult.Success

private fun List<Schema.EnvironmentsValidationResult>.areEnvironmentsValid(): Boolean =
    !any { environmentValidationResult ->
        environmentValidationResult is Schema.EnvironmentsValidationResult.Failure
    }