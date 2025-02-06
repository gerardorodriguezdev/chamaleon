package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.ui.component.Checkbox
import org.jetbrains.jewel.ui.component.Text

@Composable
fun InputCheckBox(
    label: String,
    isChecked: Boolean,
    onCheckedChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    //TODO: Common InputContainer
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        Text(text = label, modifier = Modifier.widthIn(max = 140.dp))

        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChanged,
        )
    }
}