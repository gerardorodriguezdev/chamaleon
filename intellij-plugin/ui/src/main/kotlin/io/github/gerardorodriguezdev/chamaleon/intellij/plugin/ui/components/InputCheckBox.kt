package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.jewel.ui.component.Checkbox

@Composable
fun InputCheckBox(
    label: String,
    isChecked: Boolean,
    onCheckedChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    InputContainer(
        label = label,
        modifier = modifier,
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChanged,
        )
    }
}