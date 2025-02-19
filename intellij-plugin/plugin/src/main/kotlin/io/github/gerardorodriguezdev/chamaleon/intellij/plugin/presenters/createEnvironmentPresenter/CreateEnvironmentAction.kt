package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter

import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyValue

sealed interface CreateEnvironmentAction {
    sealed interface DialogAction : CreateEnvironmentAction {
        data object OnPreviousButtonClicked : DialogAction
        data object OnNextButtonClicked : DialogAction
        data object OnFinishButtonClicked : DialogAction
    }

    sealed interface SetupEnvironmentAction : CreateEnvironmentAction {
        data object OnInit : SetupEnvironmentAction
        data object OnSelectEnvironmentPathClicked : SetupEnvironmentAction
        data class OnEnvironmentNameChanged(val newName: String) : SetupEnvironmentAction
    }

    sealed interface SetupSchemaAction : CreateEnvironmentAction {
        data class OnSupportedPlatformChanged(
            val isChecked: Boolean,
            val newPlatformType: PlatformType
        ) : SetupSchemaAction

        data object OnAddPropertyDefinitionClicked : SetupSchemaAction
        data class OnPropertyNameChanged(
            val index: Int,
            val newName: String,
        ) : SetupSchemaAction

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
        data class OnPropertyNameChanged(val index: Int, val newName: String) : SetupPropertiesAction
        data class OnPropertyValueChanged(val index: Int, val newValue: PropertyValue?) : SetupPropertiesAction
    }
}