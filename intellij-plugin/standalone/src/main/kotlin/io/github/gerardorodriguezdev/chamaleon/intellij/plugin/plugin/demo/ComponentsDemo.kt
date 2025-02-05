package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.plugin.demo

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.plugin.previews.TooltipIconButtonPreview
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Demo",
        state = rememberWindowState(width = 300.dp, height = 300.dp)
    ) {
        IntUiTheme {
            Column {
                TooltipIconButtonPreview()
            }
        }
    }
}