package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment

import androidx.compose.runtime.Composable
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.CreateEnvironmentPresenter.State
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.CreateEnvironmentPresenter.State.SelectEnvironmentPath
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.CreateEnvironmentPresenter.State.SelectSchema
import io.github.gerardorodriguezdev.chamaleon.ui.components.SomeComposable

//TODO: Finish + Test + Preview
@Composable
internal fun CreateEnvironmentWindow(state: State) {
    // TODO: Divider
    // TODO: GroupHeader
    SomeComposable()

    when (state) {
        is SelectEnvironmentPath ->
            SelectEnvironmentPathWindow(
                state = state,
                onSelectEnvironmentPathClicked = {},
            )

        is SelectSchema -> Unit
    }
}