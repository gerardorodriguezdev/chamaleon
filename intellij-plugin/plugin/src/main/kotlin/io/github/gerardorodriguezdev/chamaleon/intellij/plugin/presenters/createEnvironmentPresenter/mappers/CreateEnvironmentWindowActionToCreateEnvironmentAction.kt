package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.mappers

import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState.Platform
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowAction.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowAction.SetupEnvironmentAction.OnEnvironmentNameChanged
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowAction.SetupEnvironmentAction.OnSelectEnvironmentPath
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowAction.SetupSchemaAction.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowState

internal fun CreateEnvironmentWindowAction.toCreateEnvironmentAction(): CreateEnvironmentAction =
    when (this) {
        is SetupEnvironmentAction -> toSetupEnvironmentAction()
        is SetupSchemaAction -> toSetupSchemaAction()
        is SetupPropertiesAction -> toSetupPropertiesAction()
    }

private fun SetupEnvironmentAction.toSetupEnvironmentAction(): CreateEnvironmentAction.SetupEnvironmentAction =
    when (this) {
        is OnSelectEnvironmentPath ->
            CreateEnvironmentAction.SetupEnvironmentAction.OnSelectEnvironmentPath

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

        is OnAddPropertyDefinition -> CreateEnvironmentAction.SetupSchemaAction.OnAddPropertyDefinition

        is OnPropertyNameChanged ->
            CreateEnvironmentAction.SetupSchemaAction.OnPropertyNameChanged(index = index, newName = newName)

        is OnDeletePropertyDefinition -> CreateEnvironmentAction.SetupSchemaAction.OnDeletePropertyDefinition(index)

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
        is SetupPropertiesAction.OnPropertyValueChanged -> {
            CreateEnvironmentAction.SetupPropertiesAction.OnPropertyValueChanged(
                platformType = platformType,
                index = index,
                newValue = newValue.toPropertyValue()
            )
        }
    }

private fun CreateEnvironmentWindowState.SetupPropertiesState.Platform.Property.PropertyValue.toPropertyValue(): Platform.Property.PropertyValue =
    when (this) {
        is CreateEnvironmentWindowState.SetupPropertiesState.Platform.Property.PropertyValue.StringProperty -> Platform.Property.PropertyValue.StringProperty(
            value
        )

        is CreateEnvironmentWindowState.SetupPropertiesState.Platform.Property.PropertyValue.BooleanProperty -> Platform.Property.PropertyValue.BooleanProperty(
            value
        )

        is CreateEnvironmentWindowState.SetupPropertiesState.Platform.Property.PropertyValue.NullableBooleanProperty -> Platform.Property.PropertyValue.NullableBooleanProperty(
            value
        )
    }