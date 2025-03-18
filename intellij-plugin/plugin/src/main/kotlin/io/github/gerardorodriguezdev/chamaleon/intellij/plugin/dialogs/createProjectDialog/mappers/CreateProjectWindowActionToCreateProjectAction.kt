package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.createProjectDialog.mappers

import io.github.gerardorodriguezdev.chamaleon.core.models.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyType
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString.Companion.toNonEmptyString
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.CreateProjectAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowState

internal fun CreateProjectWindowAction.toCreateProjectAction(): CreateProjectAction? =
    when (this) {
        is CreateProjectWindowAction.SetupEnvironmentAction -> toSetupEnvironmentAction()
        is CreateProjectWindowAction.SetupSchemaAction -> toSetupSchemaAction()
        is CreateProjectWindowAction.SetupPlatformsAction -> toSetupPlatformsAction()
    }

@Suppress("MaxLineLength")
private fun CreateProjectWindowAction.SetupEnvironmentAction.toSetupEnvironmentAction(): CreateProjectAction.SetupEnvironmentAction? =
    when (this) {
        is CreateProjectWindowAction.SetupEnvironmentAction.OnSelectEnvironmentPath -> null
        is CreateProjectWindowAction.SetupEnvironmentAction.OnEnvironmentNameChanged ->
            CreateProjectAction.SetupEnvironmentAction.OnEnvironmentNameChanged(newName.toNonEmptyString())
    }

private fun CreateProjectWindowAction.SetupSchemaAction.toSetupSchemaAction(): CreateProjectAction.SetupSchemaAction? =
    when (this) {
        is CreateProjectWindowAction.SetupSchemaAction.OnGlobalSupportedPlatformTypesChanged ->
            CreateProjectAction.SetupSchemaAction.OnGlobalSupportedPlatformTypesChanged(
                isChecked = isChecked,
                newPlatformType = newPlatformType.toPlatformType(),
            )

        is CreateProjectWindowAction.SetupSchemaAction.OnAddPropertyDefinition ->
            CreateProjectAction.SetupSchemaAction.OnAddPropertyDefinition

        is CreateProjectWindowAction.SetupSchemaAction.OnDeletePropertyDefinition -> {
            CreateProjectAction.SetupSchemaAction.OnDeletePropertyDefinition(index)
        }

        is CreateProjectWindowAction.SetupSchemaAction.OnPropertyDefinitionNameChanged ->
            CreateProjectAction.SetupSchemaAction.OnPropertyDefinitionNameChanged(
                index = index,
                newPropertyName = newName.toNonEmptyString(),
            )

        is CreateProjectWindowAction.SetupSchemaAction.OnPropertyDefinitionTypeChanged ->
            CreateProjectAction.SetupSchemaAction.OnPropertyDefinitionTypeChanged(
                index = index,
                newPropertyType = newPropertyType.toPropertyType(),
            )

        is CreateProjectWindowAction.SetupSchemaAction.OnNullableChanged ->
            CreateProjectAction.SetupSchemaAction.OnNullableChanged(
                index = index,
                newNullable = newValue,
            )

        is CreateProjectWindowAction.SetupSchemaAction.OnSupportedPlatformTypesChanged ->
            CreateProjectAction.SetupSchemaAction.OnSupportedPlatformTypesChanged(
                index = index,
                isChecked = isChecked,
                newPlatformType = newPlatformType.toPlatformType(),
            )
    }

@Suppress("MaxLineLength")
private fun CreateProjectWindowAction.SetupPlatformsAction.toSetupPlatformsAction(): CreateProjectAction.SetupPlatformsAction? =
    when (this) {
        is CreateProjectWindowAction.SetupPlatformsAction.OnPropertyValueChanged ->
            CreateProjectAction.SetupPlatformsAction.OnPropertyValueChanged(
                index = index,
                platformType = platformType.toPlatformType(),
                newPropertyValue = newValue.toPropertyValue()
            )
    }

private fun CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType.toPlatformType(): PlatformType =
    when (this) {
        CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType.ANDROID -> PlatformType.ANDROID
        CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType.WASM -> PlatformType.WASM
        CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType.JS -> PlatformType.JS
        CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType.NATIVE -> PlatformType.NATIVE
        CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType.JVM -> PlatformType.JVM
    }

private fun CreateProjectWindowState.SetupSchemaState.PropertyDefinition.PropertyType.toPropertyType(): PropertyType =
    when (this) {
        CreateProjectWindowState.SetupSchemaState.PropertyDefinition.PropertyType.STRING -> PropertyType.STRING
        CreateProjectWindowState.SetupSchemaState.PropertyDefinition.PropertyType.BOOLEAN -> PropertyType.BOOLEAN
    }

@Suppress("MaxLineLength")
private fun CreateProjectWindowState.SetupPlatformsState.Platform.Property.PropertyValue.toPropertyValue(): PropertyValue? =
    when (this) {
        is CreateProjectWindowState.SetupPlatformsState.Platform.Property.PropertyValue.StringProperty ->
            valueField.value.toNonEmptyString()?.let { newValue ->
                PropertyValue.StringProperty(newValue)
            }

        is CreateProjectWindowState.SetupPlatformsState.Platform.Property.PropertyValue.BooleanProperty ->
            PropertyValue.BooleanProperty(value)

        is CreateProjectWindowState.SetupPlatformsState.Platform.Property.PropertyValue.NullableBooleanProperty ->
            value?.let { value ->
                PropertyValue.BooleanProperty(value)
            }
    }