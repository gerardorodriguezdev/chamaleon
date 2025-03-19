package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.ThemeConstants.itemsSpacing
import org.jetbrains.jewel.ui.component.Text

@Composable
internal fun Toolbar(
    modifier: Modifier = Modifier,
    title: String? = null,
    trailingIcons: @Composable (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(itemsSpacing),
        modifier = modifier.fillMaxWidth(),
    ) {
        title?.let {
            Text(text = title, modifier = Modifier.weight(1f, fill = false))
        }

        trailingIcons?.invoke()
    }
}