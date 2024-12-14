package io.github.gerardorodriguezdev.chamaleon.intellij.plugin

import androidx.compose.foundation.text.input.rememberTextFieldState
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import org.jetbrains.jewel.bridge.addComposeTab
import org.jetbrains.jewel.bridge.theme.SwingBridgeTheme
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.ui.component.TextField

class MainToolWindowFactory : ToolWindowFactory {
    @OptIn(ExperimentalJewelApi::class)
    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow
    ) {
        toolWindow.addComposeTab("Chamaleon") {
            SwingBridgeTheme {
                val textFieldState = rememberTextFieldState("Something")
                TextField(state = textFieldState)
            }
        }
    }
}