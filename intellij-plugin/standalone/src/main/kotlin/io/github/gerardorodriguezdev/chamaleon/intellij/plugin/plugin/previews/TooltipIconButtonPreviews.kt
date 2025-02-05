package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.plugin.previews

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.plugin.previews.utils.FakeIconKey
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.plugin.ui.components.TooltipIconButton

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