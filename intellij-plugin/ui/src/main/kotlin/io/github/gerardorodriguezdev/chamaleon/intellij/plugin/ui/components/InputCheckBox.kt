package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        labelWidth = 0.dp,
        modifier = modifier,
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChanged,
        )
    }
}