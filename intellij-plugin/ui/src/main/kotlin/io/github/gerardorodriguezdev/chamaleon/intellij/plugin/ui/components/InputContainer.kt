package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.ThemeConstants.labelSpacing

@Composable
internal fun InputContainer(
    label: String,
    modifier: Modifier = Modifier,
    forceLabelWidth: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(labelSpacing),
        modifier = modifier,
    ) {
        Label(label = label, forceLabelWidth = forceLabelWidth)

        content()
    }
}