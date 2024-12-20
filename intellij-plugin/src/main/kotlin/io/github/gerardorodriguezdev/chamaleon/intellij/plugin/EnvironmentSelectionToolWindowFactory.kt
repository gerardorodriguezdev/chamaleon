package io.github.gerardorodriguezdev.chamaleon.intellij.plugin

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import io.github.gerardorodriguezdev.chamaleon.core.DefaultEnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.messages.Bundle
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.EnvironmentSelectionPresenter
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.EnvironmentSelection
import kotlinx.coroutines.Dispatchers
import org.jetbrains.jewel.bridge.addComposeTab
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import java.io.File

class EnvironmentSelectionToolWindowFactory : ToolWindowFactory, Disposable {
    private val environmentSelectionPresenter = EnvironmentSelectionPresenter(
        environmentsProcessor = DefaultEnvironmentsProcessor(),
        uiDispatcher = Dispatchers.EDT,
        ioDispatcher = Dispatchers.IO,
        onPropertiesFileChanged = { propertiesFile -> propertiesFile.onPropertiesFileChanged() },
    )

    @OptIn(ExperimentalJewelApi::class)
    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow
    ) {
        project.scanProject()

        toolWindow.addComposeTab(Bundle.environmentSelectionWindowName) {
            EnvironmentSelection(
                state = environmentSelectionPresenter.state.value,
                onSelectedEnvironmentChanged = { propertiesFilePath, newSelectedEnvironment ->
                    project.onSelectedEnvironmentChanged(propertiesFilePath, newSelectedEnvironment)
                },
            )
        }
    }

    private fun Project.scanProject() {
        val projectDirectory = basePath ?: return
        environmentSelectionPresenter.scanProject(projectDirectory)
    }

    private fun Project.onSelectedEnvironmentChanged(propertiesFilePath: String, newSelectedEnvironment: String?) {
        val projectDirectory = basePath ?: return
        environmentSelectionPresenter.onEnvironmentChanged(projectDirectory, propertiesFilePath, newSelectedEnvironment)
    }

    private fun File.onPropertiesFileChanged() {
        VfsUtil.markDirtyAndRefresh(true, true, true, this)
    }

    override fun dispose() {
        environmentSelectionPresenter.dispose()
    }
}