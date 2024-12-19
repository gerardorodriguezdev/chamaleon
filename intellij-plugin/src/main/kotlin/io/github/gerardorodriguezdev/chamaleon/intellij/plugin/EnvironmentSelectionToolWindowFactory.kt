package io.github.gerardorodriguezdev.chamaleon.intellij.plugin

import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import io.github.gerardorodriguezdev.chamaleon.core.DefaultEnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.messages.Bundle
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.EnvironmentSelectionPresenter
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.EnvironmentSelection
import kotlinx.coroutines.Dispatchers
import org.jetbrains.jewel.bridge.addComposeTab
import org.jetbrains.jewel.foundation.ExperimentalJewelApi

class EnvironmentSelectionToolWindowFactory : ToolWindowFactory {
    @OptIn(ExperimentalJewelApi::class)
    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow
    ) {
        val presenter = EnvironmentSelectionPresenter(
            environmentsProcessor = DefaultEnvironmentsProcessor(),
            uiDispatcher = Dispatchers.EDT,
            backgroundDispatcher = Dispatchers.IO,
        )

        toolWindow.addComposeTab(Bundle.environmentSelectionWindowName) {
            EnvironmentSelection(
                state = presenter.state.value,
                onEnvironmentChanged = { environmentPath, newSelectedEnvironment ->
                    presenter.onEnvironmentChanged(project.basePath, environmentPath, newSelectedEnvironment)
                },
            )
        }
    }
}