package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment

import androidx.compose.runtime.Composable
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyType
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.Action.OnSelectEnvironmentPathClicked
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.Action.OnSupportedPlatformTypeChanged
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SelectEnvironmentsDirectoryLocationState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SetupSchemaState
import kotlinx.collections.immutable.ImmutableList

//TODO: Finish
@Composable
fun CreateEnvironmentWindow(
    state: State,
    onAction: (action: Action) -> Unit,
) {
    when (state) {
        is SelectEnvironmentsDirectoryLocationState ->
            SelectEnvironmentsDirectoryLocationWindow(
                state = state,
                onIconClicked = {
                    onAction(OnSelectEnvironmentPathClicked)
                },
            )

        is SetupSchemaState ->
            SetupSchemaWindow(
                state = state,
                onSupportedPlatformsChanged = { platformType ->
                    OnSupportedPlatformTypeChanged(platformType)
                }
            )
    }
}

sealed interface State {
    data class SelectEnvironmentsDirectoryLocationState(
        val path: String,
        val verification: Verification?,
    ) : State {
        sealed interface Verification {
            data object Valid : Verification
            data class Invalid(val reason: String) : Verification
            data object InProgress : Verification
        }
    }

    data class SetupSchemaState(
        val title: String,
        val supportedPlatforms: ImmutableList<PlatformType>,
        val propertyDefinitions: ImmutableList<PropertyDefinition>,
    ) : State {

        data class PropertyDefinition(
            val name: String,
            val propertyType: PropertyType,
            val nullable: Boolean,
            val supportedPlatforms: ImmutableList<PlatformType>,
        )
    }
}

sealed interface Action {
    data object OnSelectEnvironmentPathClicked : Action
    data class OnSupportedPlatformTypeChanged(val platformType: PlatformType) : Action
}