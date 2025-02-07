package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyValue
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.*
import kotlinx.collections.immutable.ImmutableList

//TODO: Finish
@Composable
fun CreateEnvironmentWindow(
    state: State,
    onAction: (action: Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is SelectEnvironmentsDirectoryLocationState ->
            SelectEnvironmentsDirectoryLocationWindow(
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
            )
    }
}

sealed interface State {
    val isPreviousButtonEnabled: Boolean
    val isNextButtonEnabled: Boolean
    val isFinishButtonEnabled: Boolean

    data class SelectEnvironmentsDirectoryLocationState(
        val path: String,
        val verification: Verification?,
        override val isNextButtonEnabled: Boolean = false,
    ) : State {
        override val isPreviousButtonEnabled: Boolean = false
        override val isFinishButtonEnabled: Boolean = false

        sealed interface Verification {
            data object Valid : Verification
            data class Invalid(val reason: String) : Verification
            data object InProgress : Verification
        }
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
        val environmentName: String,
        val properties: ImmutableList<Property>,
        override val isFinishButtonEnabled: Boolean = true,
    ) : State {
        override val isPreviousButtonEnabled: Boolean = true
        override val isNextButtonEnabled: Boolean = false

        data class Property(
            val name: String,
            val value: PropertyValue,
        )
    }
}

sealed interface Action {
    data object OnPreviousButtonClicked : Action
    data object OnNextButtonClicked : Action
    data object OnFinishButtonClicked : Action

    sealed interface SelectEnvironmentsDirectoryLocationAction : Action {
        data object OnSelectEnvironmentPathClicked : SelectEnvironmentsDirectoryLocationAction
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
}