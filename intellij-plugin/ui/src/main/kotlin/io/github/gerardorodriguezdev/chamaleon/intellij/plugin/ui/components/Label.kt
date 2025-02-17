package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.ThemeConstants.labelWidth
import org.jetbrains.jewel.ui.component.Text

@Composable
internal fun Label(label: String, forceLabelWidth: Boolean = true) {
    Text(text = label, modifier = if (forceLabelWidth) Modifier.width(labelWidth) else Modifier)
}