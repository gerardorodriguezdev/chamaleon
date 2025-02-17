package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.mappers

import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.BaseDialog.DialogAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.BaseDialog.DialogAction.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState.EnvironmentNameVerification
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState.Step
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
                path = environmentsDirectoryPathField.value,
                environmentsDirectoryVerification = environmentsDirectoryPathField.verification,

                environmentName = environmentName,
                environmentNameVerification = environmentNameVerification.toEnvironmentNameVerification(),
            )
        }

        Step.SETUP_SCHEMA -> {
            //TODO: Update
            SetupSchemaState(
                title = "Update",
                supportedPlatforms = persistentListOf(),
                propertyDefinitions = persistentListOf(),
            )
        }
    }

private fun EnvironmentNameVerification.toEnvironmentNameVerification(): SetupEnvironmentState.EnvironmentNameVerification =
    when (this) {
        EnvironmentNameVerification.VALID -> SetupEnvironmentState.EnvironmentNameVerification.VALID
        EnvironmentNameVerification.IS_EMPTY -> SetupEnvironmentState.EnvironmentNameVerification.IS_EMPTY
        EnvironmentNameVerification.IS_DUPLICATED -> SetupEnvironmentState.EnvironmentNameVerification.IS_DUPLICATED
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