@file:Suppress("InvalidPackageDeclaration")

package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.gerardorodriguezdev.chamaleon.core.entities.Platform.Property
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyValue
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.Verification
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.LoadingWindow
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.*
import kotlinx.collections.immutable.ImmutableList

@Composable
fun CreateEnvironmentWindow(
    state: State,
    onAction: (action: Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is LoadingState -> LoadingWindow(modifier = modifier)
        is SetupEnvironmentState ->
            SetupEnvironmentWindow(
                state = state,
                onAction = onAction,
                modifier = modifier,
            )

        is SetupSchemaState ->
            SetupSchemaWindow(
                state = state,
                onAction = onAction,
            )

        is SetupPropertiesState ->
            SetupPropertiesWindow(
                state = state,
                onAction = onAction,
            )
    }
}

sealed interface State {
    val isPreviousButtonEnabled: Boolean
    val isNextButtonEnabled: Boolean
    val isFinishButtonEnabled: Boolean

    data class LoadingState(
        override val isPreviousButtonEnabled: Boolean = false,
    ) : State {
        override val isNextButtonEnabled: Boolean = false
        override val isFinishButtonEnabled: Boolean = false
    }

    data class SetupEnvironmentState(
        val path: String,
        val verification: Verification?,
        override val isNextButtonEnabled: Boolean = false,
    ) : State {
        override val isPreviousButtonEnabled: Boolean = false
        override val isFinishButtonEnabled: Boolean = false
    }

    data class SetupSchemaState(
        val title: String,
        val supportedPlatforms: ImmutableList<SupportedPlatform>,
        val propertyDefinitions: ImmutableList<PropertyDefinition>,
        override val isNextButtonEnabled: Boolean = false,
    ) : State {
        override val isPreviousButtonEnabled: Boolean = true
        override val isFinishButtonEnabled: Boolean = false

        data class SupportedPlatform(
            val isChecked: Boolean,
            val platformType: PlatformType,
        )

        data class PropertyDefinition(
            val name: String,
            val propertyType: PropertyType,
            val nullable: Boolean,
            val supportedPlatforms: ImmutableList<SupportedPlatform>,
        )
    }

    data class SetupPropertiesState(
        val platforms: ImmutableList<Platform>,
        override val isFinishButtonEnabled: Boolean = true,
    ) : State {
        override val isPreviousButtonEnabled: Boolean = true
        override val isNextButtonEnabled: Boolean = false

        data class Platform(
            val platformType: PlatformType,
            val properties: ImmutableList<Property>,
        )
    }
}

sealed interface Action {
    sealed interface ExternalAction : Action {
        data object OnPreviousButtonClicked : ExternalAction
        data object OnNextButtonClicked : ExternalAction
        data object OnFinishButtonClicked : ExternalAction
    }

    sealed interface SetupEnvironmentAction : Action {
        data object OnSelectEnvironmentPathClicked : SetupEnvironmentAction
        data class OnEnvironmentNameChanged(val newName: String) : SetupEnvironmentAction
    }

    sealed interface SetupSchemaAction : Action {
        data class OnSupportedPlatformChanged(val newPlatformType: PlatformType) : SetupSchemaAction
        data object OnAddPropertyDefinitionClicked : SetupSchemaAction
        data class OnPropertyNameChanged(val index: Int, val newName: String) : SetupSchemaAction
        data class OnPropertyTypeChanged(val index: Int, val newPropertyType: PropertyType) : SetupSchemaAction
        data class OnNullableChanged(val index: Int, val newValue: Boolean) : SetupSchemaAction
        data class OnPropertyDefinitionSupportedPlatformChanged(val index: Int, val newPlatformType: PlatformType) :
            SetupSchemaAction
    }

    sealed interface SetupPropertiesAction : Action {
        data class OnPropertyNameChanged(val index: Int, val newName: String) : SetupPropertiesAction
        data class OnPropertyValueChanged(val index: Int, val newValue: PropertyValue) : SetupPropertiesAction
    }
}