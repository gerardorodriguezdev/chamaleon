package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyType
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.Action.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SelectEnvironmentsDirectoryLocationState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SetupSchemaState
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
                onIconClicked = {
                    onAction(OnSelectEnvironmentPathClicked)
                },
                modifier = modifier,
            )

        is SetupSchemaState ->
            SetupSchemaWindow(
                state = state,
                onSupportedPlatformsChanged = { platformType ->
                    onAction(OnSupportedPlatformTypeChanged(platformType))
                },
                onAddPropertyDefinitionClicked = {
                    onAction(OnAddPropertyDefinitionClicked)
                }
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
}

sealed interface Action {
    data object OnPreviousButtonClicked : Action
    data object OnNextButtonClicked : Action
    data object OnFinishButtonClicked : Action

    data object OnSelectEnvironmentPathClicked : Action

    //TODO: Sep actions to window type emission
    data class OnSupportedPlatformTypeChanged(val platformType: PlatformType) : Action
    data object OnAddPropertyDefinitionClicked : Action
}