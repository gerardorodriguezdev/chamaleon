package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField

@Composable
fun InputField(
    label: String,
    initialValue: String = "",
    onValueChange: (newValue: String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        Text(text = label, modifier = Modifier.widthIn(min = 140.dp))

        val textFieldState = rememberTextFieldState(initialValue)
        TextField(state = textFieldState)

        snapshotFlow {
            onValueChange(textFieldState.text.toString())
        }
    }
}