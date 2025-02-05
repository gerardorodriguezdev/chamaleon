package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.demo

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.previews.windows.createEnvironment.SelectEnvironmentsDirectoryLocationWindowPreview
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme

internal fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Demo",
        state = rememberWindowState(width = 300.dp, height = 300.dp)
    ) {
        IntUiTheme {
            Column {
                SelectEnvironmentsDirectoryLocationWindowPreview()
            }
        }
    }
}