package io.github.gerardorodriguezdev.chamaleon.intellij.plugin

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.EnvironmentSelectionPresenter
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings.BundleStringsProvider.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.theme.PluginTheme.Theme
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.theme.PluginTheme.stringsProvider
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.EnvironmentSelectionWindow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import org.jetbrains.jewel.bridge.addComposeTab
import org.jetbrains.jewel.bridge.theme.SwingBridgeTheme
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import java.io.File

internal class EnvironmentSelectionToolWindowFactory : ToolWindowFactory, Disposable {
    private val environmentsProcessor = EnvironmentsProcessor.create()
    private val environmentSelectionPresenter = EnvironmentSelectionPresenter(
        stringsProvider = stringsProvider,
        environmentsProcessor = environmentsProcessor,
        uiDispatcher = Dispatchers.Swing,
        ioDispatcher = Dispatchers.IO,
        onEnvironmentsDirectoryChanged = { environmentsDirectory ->
            environmentsDirectory.onEnvironmentsDirectoryChanged()
        },
    )

    @OptIn(ExperimentalJewelApi::class)
    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow
    ) {
        project.scanProject()

        toolWindow.addComposeTab(tabDisplayName = string(StringsKeys.environmentSelectionWindowName)) {
            SwingBridgeTheme {
                Theme {
                    EnvironmentSelectionWindow(
                        state = environmentSelectionPresenter.state.value,
                        onRefresh = {
                            project.scanProject()
                        },
                        onCreateEnvironment = {
                            val projectDirectoryPath = project.basePath ?: return@EnvironmentSelectionWindow

                            EnvironmentCreationDialog(
                                project = project,
                                projectDirectory = File(projectDirectoryPath),
                                environmentsProcessor = environmentsProcessor,
                            ).show()
                        },
                        onSelectedEnvironmentChanged = { environmentsDirectoryPath, newSelectedEnvironment ->
                            project.onSelectedEnvironmentChanged(environmentsDirectoryPath, newSelectedEnvironment)
                        },
                    )
                }
            }
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