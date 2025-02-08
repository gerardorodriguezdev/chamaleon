package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.ThemeConstants.itemsSpacing
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Divider

@Suppress("LongParameterList")
@Composable
fun Section(
    modifier: Modifier = Modifier,
    title: String? = null,
    titleTrailingIcon: @Composable (() -> Unit)? = null,
    forceLabelWidth: Boolean = true,
    enableDivider: Boolean = false,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(itemsSpacing),
        modifier = modifier,
        content = {
            if (enableDivider) {
                Divider(orientation = Orientation.Horizontal)
            }

            if (title != null || titleTrailingIcon != null) {
                Toolbar(title = title, trailingIcons = titleTrailingIcon, forceLabelWidth = forceLabelWidth)
            }

            content()
        },
    )
}