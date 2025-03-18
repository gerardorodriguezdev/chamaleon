package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.jewel.ui.component.IconActionButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Tooltip
import org.jetbrains.jewel.ui.icon.IconKey

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TooltipIconButton(
    iconKey: IconKey,
    tooltip: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Tooltip(
        modifier = modifier,
        tooltip = {
            Text(text = tooltip)
        },
        content = {
            IconActionButton(
                key = iconKey,
                onClick = onClick,
                contentDescription = tooltip,
            )
        }
    )
}