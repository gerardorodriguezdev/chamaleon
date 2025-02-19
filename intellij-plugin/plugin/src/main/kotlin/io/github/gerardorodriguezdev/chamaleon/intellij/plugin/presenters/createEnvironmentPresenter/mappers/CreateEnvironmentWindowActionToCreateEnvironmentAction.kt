package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.mappers

import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowAction.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowAction.SetupEnvironmentAction.OnEnvironmentNameChanged
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowAction.SetupEnvironmentAction.OnSelectEnvironmentPathClicked
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowAction.SetupSchemaAction.*

internal fun CreateEnvironmentWindowAction.toCreateEnvironmentAction(): CreateEnvironmentAction =
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
            CreateEnvironmentAction.SetupSchemaAction.OnSupportedPlatformChanged(
                isChecked = isChecked,
                newPlatformType = newPlatformType
            )

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
                isChecked = isChecked,
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