package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.LocalStrings
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.TooltipIconButton
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SelectEnvironmentPath
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.icons.AllIconsKeys

// TODO: Finish + Test + Preview
@Composable
fun SelectEnvironmentPathWindow(
    state: SelectEnvironmentPath,
    onSelectEnvironmentPathClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = LocalStrings.current.environmentsDirectoryLocation, modifier = Modifier.widthIn(max = 140.dp))

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
                },
            )
        }
    }
}