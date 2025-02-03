package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.plugin.previews

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.plugin.ui.components.SomeComposable
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme

//TODO: Remove
@Preview
@Composable
private fun SomeComposablePreview() {
    IntUiTheme {
        SomeComposable()
    }
}