package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.previews.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.previews.utils.FakeIconKey
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.previews.utils.PreviewContainer
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.TooltipIconButton

@Preview
@Composable
internal fun TooltipIconButtonPreview() {
    PreviewContainer {
        TooltipIconButton(
            iconKey = FakeIconKey,
            tooltip = "This is a tooltip",
            onClick = {}
        )
    }
}