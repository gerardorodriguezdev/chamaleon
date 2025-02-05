package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment

import androidx.compose.runtime.Composable
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema.PropertyDefinition
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SelectEnvironmentsDirectoryLocationState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SetupSchemaState
import kotlinx.collections.immutable.ImmutableSet

// TODO: Finish + Test + Preview
@Composable
fun CreateEnvironmentWindow(state: State) {
    when (state) {
        is SelectEnvironmentsDirectoryLocationState ->
            SelectEnvironmentsDirectoryLocationWindow(
                state = state,
                onIconClicked = {},
            )

        is SetupSchemaState ->
            SetupSchema(state = state)
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
        val supportedPlatforms: ImmutableSet<PlatformType>,
        val propertyDefinitions: ImmutableSet<PropertyDefinition>,
    ) : State
}

sealed interface Action {
    data object OnSelectEnvironmentPathClicked : Action
}