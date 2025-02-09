package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Tooltip
import org.jetbrains.jewel.ui.icon.IconKey

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TooltipIcon(iconKey: IconKey, tooltip: String, modifier: Modifier = Modifier) {
    Tooltip(
        tooltip = {
            Text(text = tooltip)
        },
        content = {
            Icon(
                key = iconKey,
                contentDescription = tooltip,
                modifier = modifier,
            )
        }
    )
}