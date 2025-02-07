package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun InputTextDropdown(
    label: String,
    selectedValue: String,
    content: TextDropdownScope.() -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    InputContainer(label = label, modifier = modifier) {
        TextDropdown(
            selectedValue = selectedValue,
            content = content,
        )

        trailingIcon?.invoke()
    }
}