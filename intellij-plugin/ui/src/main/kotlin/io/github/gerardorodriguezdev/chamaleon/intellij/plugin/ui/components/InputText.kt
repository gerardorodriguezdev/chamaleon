package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.ThemeConstants.itemsSpacing
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.ThemeConstants.labelWidth
import org.jetbrains.jewel.ui.component.Text

@Composable
internal fun InputText(
    label: String,
    text: String,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    InputContainer(label = label, modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(itemsSpacing),
        ) {
            Text(
                text = text,
                modifier = Modifier
                    .widthIn(min = labelWidth)
                    .weight(weight = 1f, fill = false),
            )

            trailingIcon?.invoke()
        }
    }
}