package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.TooltipIconButton
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SelectEnvironmentPath
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.icons.AllIconsKeys

// TODO: Finish + Test + Preview
@Composable
fun SelectEnvironmentPathWindow(
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