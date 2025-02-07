package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun InputText(
    label: String,
    text: String,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    InputContainer(label = label, modifier = modifier) {
        Label(label = text)

        trailingIcon?.invoke()
    }
}