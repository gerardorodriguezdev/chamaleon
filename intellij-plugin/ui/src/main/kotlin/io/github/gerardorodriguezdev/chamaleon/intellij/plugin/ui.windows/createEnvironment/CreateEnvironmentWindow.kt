package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment

import androidx.compose.runtime.Composable
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SelectEnvironmentPath
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SelectSchema

// TODO: Finish + Test + Preview
@Composable
fun CreateEnvironmentWindow(state: State) {
    // TODO: Divider
    // TODO: GroupHeader

    when (state) {
        is SelectEnvironmentPath ->
            SelectEnvironmentPathWindow(
                state = state,
                onSelectEnvironmentPathClicked = {},
            )

        is SelectSchema -> Unit
    }
}

sealed interface State {
    data class SelectEnvironmentPath(val name: String) : State
    data class SelectSchema(val name: String, val schema: String) : State
}

sealed interface Action {
    data object OnSelectEnvironmentPathClicked : Action
}