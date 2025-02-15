package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.jewel.ui.component.TextField

@Suppress("LongParameterList")
@Composable
fun InputTextField(
    label: String,
    modifier: Modifier = Modifier,
    value: String = "",
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    onValueChange: (newText: String) -> Unit = {},
) {
    InputContainer(
        label = label,
        modifier = modifier.fillMaxWidth(),
    ) {
        TextField(
            value = value,
            trailingIcon = trailingIcon,
            readOnly = readOnly,
            onValueChange = onValueChange,
        )
    }
}