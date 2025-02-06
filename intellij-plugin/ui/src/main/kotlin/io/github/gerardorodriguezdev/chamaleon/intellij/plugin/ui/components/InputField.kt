package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import org.jetbrains.jewel.ui.component.TextField

@Composable
fun InputField(
    label: String,
    initialValue: String = "",
    onValueChange: (newValue: String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    InputContainer(
        label = label,
        modifier = modifier,
    ) {
        val textFieldState = rememberTextFieldState(initialValue)
        TextField(state = textFieldState)

        LaunchedEffect(Unit) {
            snapshotFlow {
                onValueChange(textFieldState.text.toString())
            }
        }
    }
}