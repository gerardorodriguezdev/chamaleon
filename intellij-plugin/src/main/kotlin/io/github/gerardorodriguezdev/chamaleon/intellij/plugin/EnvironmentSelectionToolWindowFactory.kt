package io.github.gerardorodriguezdev.chamaleon.intellij.plugin

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.messages.Bundle
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.EnvironmentSelectionPresenter
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.EnvironmentSelection
import kotlinx.coroutines.Dispatchers
import org.jetbrains.jewel.bridge.addComposeTab
import java.io.File

class EnvironmentSelectionToolWindowFactory : ToolWindowFactory, Disposable {
    private val environmentSelectionPresenter = EnvironmentSelectionPresenter(
        environmentsProcessor = EnvironmentsProcessor.create(),
        uiDispatcher = Dispatchers.EDT,
        ioDispatcher = Dispatchers.IO,
        onEnvironmentsDirectoryChanged = { environmentsDirectory ->
            environmentsDirectory.onEnvironmentsDirectoryChanged()
        },
    )

    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow
    ) {
        project.scanProject()

        toolWindow.addComposeTab(Bundle.environmentSelectionWindowName) {
            EnvironmentSelection(
                state = environmentSelectionPresenter.state.value,
                onRefreshClicked = {
                    project.scanProject()
                },
                onSelectedEnvironmentChanged = { environmentsDirectoryPath, newSelectedEnvironment ->
                    project.onSelectedEnvironmentChanged(environmentsDirectoryPath, newSelectedEnvironment)
                },
            )
        }
    }

    private fun Project.scanProject() {
        val projectDirectoryPath = basePath ?: return
        val projectDirectory = File(projectDirectoryPath)
        environmentSelectionPresenter.scanProject(projectDirectory)
    }

    private fun Project.onSelectedEnvironmentChanged(
        environmentsDirectoryPath: String,
        newSelectedEnvironment: String?
    ) {
        val projectDirectoryPath = basePath ?: return
        val projectDirectory = File(projectDirectoryPath)
        environmentSelectionPresenter.onSelectedEnvironmentChanged(
            projectDirectory = projectDirectory,
            environmentsDirectoryPath = environmentsDirectoryPath,
            newSelectedEnvironment = newSelectedEnvironment,
        )
    }

    private fun File.onEnvironmentsDirectoryChanged() {
        VfsUtil.markDirtyAndRefresh(true, true, true, this)
    }

    override fun dispose() {
        environmentSelectionPresenter.dispose()
    }
}