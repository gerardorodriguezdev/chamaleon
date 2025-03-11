package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter

import io.github.gerardorodriguezdev.chamaleon.core.models.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyType
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString

sealed interface CreateProjectAction {
    sealed interface SetupEnvironmentAction : CreateProjectAction {
        data class OnEnvironmentsDirectoryChanged(
            val newEnvironmentsDirectory: ExistingDirectory
        ) : SetupEnvironmentAction

        data class OnEnvironmentNameChanged(val newEnvironmentName: NonEmptyString) : SetupEnvironmentAction
    }

    sealed interface SetupSchemaAction : CreateProjectAction {
        data class OnGlobalSupportedPlatformTypesChanged(
            val isChecked: Boolean,
            val newPlatformType: PlatformType
        ) : SetupSchemaAction

        data object OnAddPropertyDefinition : SetupSchemaAction

        data class OnPropertyDefinitionNameChanged(
            val index: Int,
            val newPropertyName: NonEmptyString,
        ) : SetupSchemaAction

        data class OnDeletePropertyDefinition(val index: Int) : SetupSchemaAction

        data class OnPropertyDefinitionTypeChanged(
            val index: Int,
            val newPropertyType: PropertyType
        ) : SetupSchemaAction

        data class OnNullableChanged(
            val index: Int,
            val newNullable: Boolean,
        ) : SetupSchemaAction

        data class OnSupportedPlatformTypesChanged(
            val index: Int,
            val isChecked: Boolean,
            val newPlatformType: PlatformType
        ) : SetupSchemaAction
    }

    sealed interface SetupPlatformsAction : CreateProjectAction {
        data class OnPropertyValueChanged(
            val platformType: PlatformType,
            val index: Int,
            val newPropertyValue: PropertyValue?,
        ) : SetupPlatformsAction
    }

    sealed interface NavigationAction : CreateProjectAction {
        data object OnPrevious : NavigationAction
        data object OnNext : NavigationAction
        data object OnFinish : NavigationAction
    }
}