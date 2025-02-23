package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter

import io.github.gerardorodriguezdev.chamaleon.core.models.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyType
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState.Platform.Property.PropertyValue

internal sealed interface CreateEnvironmentAction {
    sealed interface DialogAction : CreateEnvironmentAction {
        data object OnPreviousButtonClicked : DialogAction
        data object OnNextButtonClicked : DialogAction
        data object OnFinishButtonClicked : DialogAction
    }

    sealed interface SetupEnvironmentAction : CreateEnvironmentAction {
        data object OnInit : SetupEnvironmentAction
        data object OnSelectEnvironmentPath : SetupEnvironmentAction
        data class OnEnvironmentNameChanged(val newName: String) : SetupEnvironmentAction
    }

    sealed interface SetupSchemaAction : CreateEnvironmentAction {
        data class OnSupportedPlatformChanged(
            val isChecked: Boolean,
            val newPlatformType: PlatformType
        ) : SetupSchemaAction

        data object OnAddPropertyDefinition : SetupSchemaAction

        data class OnPropertyNameChanged(
            val index: Int,
            val newName: String,
        ) : SetupSchemaAction

        data class OnDeletePropertyDefinition(val index: Int) : SetupSchemaAction

        data class OnPropertyTypeChanged(
            val index: Int,
            val newPropertyType: PropertyType
        ) : SetupSchemaAction

        data class OnNullableChanged(
            val index: Int,
            val newValue: Boolean,
        ) : SetupSchemaAction

        data class OnPropertyDefinitionSupportedPlatformChanged(
            val index: Int,
            val isChecked: Boolean,
            val newPlatformType: PlatformType
        ) : SetupSchemaAction
    }

    sealed interface SetupPropertiesAction : CreateEnvironmentAction {
        data class OnPropertyValueChanged(
            val platformType: PlatformType,
            val index: Int,
            val newValue: PropertyValue,
        ) : SetupPropertiesAction
    }
}