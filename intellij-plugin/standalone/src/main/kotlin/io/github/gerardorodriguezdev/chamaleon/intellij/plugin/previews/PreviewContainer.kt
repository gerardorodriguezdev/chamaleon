package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.previews

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.BaseTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
internal fun PreviewContainer(
    isDark: Boolean = true,
    backgroundColor: Color = if (isDark) Color.White else Color.Black,
    content: @Composable () -> Unit,
) {
    IntUiTheme(isDark = isDark) {
        BaseTheme {
            Box(modifier = Modifier.background(backgroundColor)) {
                content()
            }
        }
    }
}

@Preview
@Composable
private fun PreviewContainerPreview() {
    PreviewContainer {
        Text(text = "SomeText")
    }
}