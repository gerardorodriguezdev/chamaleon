package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import org.jetbrains.jewel.ui.component.TextField

@Composable
fun InputTextField(
    label: String,
    modifier: Modifier = Modifier,
    initialValue: String = "",
    readOnly: Boolean = false,
    onValueChange: (newValue: String) -> Unit = {},
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    InputContainer(
        label = label,
        modifier = modifier.fillMaxWidth(),
    ) {
        val textFieldState = rememberTextFieldState(initialValue)
        TextField(state = textFieldState, trailingIcon = trailingIcon, readOnly = readOnly)

        LaunchedEffect(Unit) {
            snapshotFlow {
                onValueChange(textFieldState.text.toString())
            }
        }
    }
}