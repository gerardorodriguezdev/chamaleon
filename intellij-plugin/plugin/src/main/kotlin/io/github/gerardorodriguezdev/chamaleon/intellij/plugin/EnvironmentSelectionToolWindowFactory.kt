package io.github.gerardorodriguezdev.chamaleon.intellij.plugin

import androidx.compose.runtime.mutableStateOf
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import io.github.gerardorodriguezdev.chamaleon.core.Versions
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory.Companion.toExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString.Companion.toNonEmptyString
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectDeserializer
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectSerializer
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.createProjectDialog.CreateProjectDialog
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.environmentSelectionPresenter.EnvironmentSelectionAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.environmentSelectionPresenter.EnvironmentSelectionPresenter
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings.BundleStringsProvider.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.theme.PluginTheme.Theme
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.theme.PluginTheme.stringsProvider
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.EnvironmentSelectionWindow
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.EnvironmentSelectionWindowState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.utils.notifyDirectoryChanged
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import org.jetbrains.jewel.bridge.addComposeTab
import org.jetbrains.jewel.bridge.theme.SwingBridgeTheme
import org.jetbrains.jewel.foundation.ExperimentalJewelApi

internal class EnvironmentSelectionToolWindowFactory : ToolWindowFactory, Disposable {
    private val projectSerializer = ProjectSerializer.create()
    private val projectDeserializer = ProjectDeserializer.create()

    private val environmentSelectionPresenter = EnvironmentSelectionPresenter(
        stringsProvider = stringsProvider,
        uiDispatcher = Dispatchers.Swing,
        ioDispatcher = Dispatchers.IO,
        projectSerializer = projectSerializer,
        projectDeserializer = projectDeserializer,
        onEnvironmentsDirectoryChanged = { environmentsDirectory ->
            environmentsDirectory.notifyDirectoryChanged()
        },
    )

    private val environmentSelectionWindowState =
        mutableStateOf<EnvironmentSelectionWindowState>(
            EnvironmentSelectionWindowState(gradlePluginVersionUsed = Versions.CORE)
        )

    @OptIn(ExperimentalJewelApi::class)
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        project.scanProject()

        toolWindow.addComposeTab(tabDisplayName = string(StringsKeys.environmentSelectionWindowName)) {
            SwingBridgeTheme {
                Theme {
                    EnvironmentSelectionWindow(
                        state = environmentSelectionWindowState.value,
                        onRefresh = {
                            project.scanProject()
                        },
                        onCreateProject = {
                            val projectDirectoryPath = project.basePath ?: return@EnvironmentSelectionWindow
                            val projectDirectory =
                                projectDirectoryPath.toExistingDirectory() ?: return@EnvironmentSelectionWindow

                            CreateProjectDialog(
                                project = project,
                                projectDirectory = projectDirectory,
                                projectDeserializer = projectDeserializer,
                            ).show()
                        },
                        onSelectedEnvironmentChanged = { environmentsDirectoryPath, newSelectedEnvironment ->
                            val environmentsDirectoryPath =
                                environmentsDirectoryPath.toNonEmptyString() ?: return@EnvironmentSelectionWindow
                            val newSelectedEnvironment = newSelectedEnvironment?.toNonEmptyString()
                            project.onSelectedEnvironmentChanged(environmentsDirectoryPath, newSelectedEnvironment)
                        },
                    )
                }
            }
        }
    }

    private fun Project.scanProject() {
        val projectDirectoryPath = basePath ?: return
        val projectDirectory = projectDirectoryPath.toExistingDirectory() ?: return
        environmentSelectionPresenter.dispatch(EnvironmentSelectionAction.ScanProject(projectDirectory))
    }

    private fun Project.onSelectedEnvironmentChanged(
        environmentsDirectoryPath: NonEmptyString,
        newSelectedEnvironment: NonEmptyString?
    ) {
        environmentSelectionPresenter.dispatch(
            EnvironmentSelectionAction.SelectEnvironment(
                environmentsDirectoryPath = environmentsDirectoryPath,
                newSelectedEnvironment = newSelectedEnvironment,
            )
        )
    }

    override fun dispose() {
        environmentSelectionPresenter.dispose()
    }
}