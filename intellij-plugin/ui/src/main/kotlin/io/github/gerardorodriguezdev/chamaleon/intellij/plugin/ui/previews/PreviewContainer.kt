package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.previews

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
internal fun PreviewContainer(
    isDark: Boolean = true,
    backgroundColor: Color = if (isDark) Color.Black else Color.White,
    content: @Composable () -> Unit,
) {
    IntUiTheme(isDark = isDark) {
        PreviewTheme.Theme {
            Box(modifier = Modifier.background(backgroundColor)) {
                content()
            }
        }
    }
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun PreviewContainerPreview() {
    PreviewContainer {
        Text(text = "SomeText")
    }
}