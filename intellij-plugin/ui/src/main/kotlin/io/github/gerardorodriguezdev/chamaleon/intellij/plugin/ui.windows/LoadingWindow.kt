package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.WindowContainer
import org.jetbrains.jewel.ui.component.CircularProgressIndicator

@Composable
fun LoadingWindow() {
    WindowContainer {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}