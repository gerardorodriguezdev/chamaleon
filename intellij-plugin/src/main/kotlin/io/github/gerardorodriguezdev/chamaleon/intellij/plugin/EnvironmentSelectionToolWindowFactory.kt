package io.github.gerardorodriguezdev.chamaleon.intellij.plugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.messages.Bundle
import org.jetbrains.jewel.bridge.addComposeTab
import org.jetbrains.jewel.foundation.ExperimentalJewelApi

class EnvironmentSelectionToolWindowFactory : ToolWindowFactory {
    @OptIn(ExperimentalJewelApi::class)
    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow
    ) {
        toolWindow.addComposeTab(Bundle.environmentSelectionWindowName) {
            //TODO: Connect
        }
    }
}