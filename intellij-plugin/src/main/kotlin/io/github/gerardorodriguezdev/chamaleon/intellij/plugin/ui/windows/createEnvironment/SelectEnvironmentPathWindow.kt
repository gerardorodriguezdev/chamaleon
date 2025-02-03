package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.CreateEnvironmentPresenter.State.SelectEnvironmentPath
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.TooltipIconButton
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.icons.AllIconsKeys

//TODO: Finish + Test + Preview
@Composable
internal fun SelectEnvironmentPathWindow(
    state: SelectEnvironmentPath,
    onSelectEnvironmentPathClicked: () -> Unit,
) {
    val textFieldState = rememberTextFieldState(initialText = state.name)
    TextField(
        state = textFieldState,
        readOnly = true,
        trailingIcon = {
            TooltipIconButton(
                iconKey = AllIconsKeys.Actions.NewFolder,
                tooltip = state.name,
                onClick = { onSelectEnvironmentPathClicked() }
            )
        }
    )
}

@Preview
@Composable
private fun SelectEnvironmentPathWindowPreview() {
    IntUiTheme {
        SelectEnvironmentPathWindow(
            state = SelectEnvironmentPath(
                name = "",
            ),
            onSelectEnvironmentPathClicked = {},
        )
    }
}