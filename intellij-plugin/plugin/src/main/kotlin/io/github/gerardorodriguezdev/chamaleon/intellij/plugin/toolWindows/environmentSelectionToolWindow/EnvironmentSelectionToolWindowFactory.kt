package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.toolWindows.environmentSelectionToolWindow

import androidx.compose.runtime.mutableStateOf
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import io.github.gerardorodriguezdev.chamaleon.core.Versions
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString.Companion.toNonEmptyString
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectDeserializer
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectSerializer
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.createProjectDialog.CreateProjectDialog
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.environmentSelectionPresenter.EnvironmentSelectionAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.environmentSelectionPresenter.EnvironmentSelectionPresenter
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.toolWindows.environmentSelectionToolWindow.mappers.toEnvironmentsSelectionWindowState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings.BundleStringsProvider
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.theme.PluginTheme.Theme
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.theme.PluginTheme.stringsProvider
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.EnvironmentSelectionWindow
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.EnvironmentSelectionWindowState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.utils.notifyDirectoryChanged
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.utils.toExistingDirectory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import org.jetbrains.jewel.bridge.addComposeTab
import org.jetbrains.jewel.bridge.theme.SwingBridgeTheme
import org.jetbrains.jewel.foundation.ExperimentalJewelApi

internal class EnvironmentSelectionToolWindowFactory : ToolWindowFactory, Disposable {
    private val projectSerializer = ProjectSerializer.Companion.create()
    private val projectDeserializer = ProjectDeserializer.Companion.create()
    private val uiScope = CoroutineScope(Dispatchers.Swing)
    private val ioScope = CoroutineScope(Dispatchers.IO)

    private val presenter = EnvironmentSelectionPresenter(
        stringsProvider = stringsProvider,
        uiScope = uiScope,
        ioScope = ioScope,
        projectSerializer = projectSerializer,
        projectDeserializer = projectDeserializer,
        onEnvironmentsDirectoryChanged = { environmentsDirectory -> environmentsDirectory.notifyDirectoryChanged() },
    )

    private val environmentSelectionWindowState =
        mutableStateOf<EnvironmentSelectionWindowState>(
            EnvironmentSelectionWindowState(gradlePluginVersionUsed = Versions.CORE)
        )

    @OptIn(ExperimentalJewelApi::class)
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        collectState(project)

        project.scanProject()

        toolWindow.addComposeTab(tabDisplayName = BundleStringsProvider.string(StringsKeys.environmentSelectionWindowName)) {
            SwingBridgeTheme {
                Theme {
                    EnvironmentSelectionWindow(
                        state = environmentSelectionWindowState.value,
                        onRefresh = {
                            project.scanProject()
                        },
                        onCreateProject = {
                            val projectDirectory = project.toExistingDirectory() ?: return@EnvironmentSelectionWindow

                            CreateProjectDialog(
                                project = project,
                                projectDirectory = projectDirectory,
                                projectDeserializer = projectDeserializer,
                            ).show()
                        },
                        onSelectedEnvironmentChanged = { environmentsDirectoryPath, newSelectedEnvironment ->
                            project.onSelectedEnvironmentChanged(
                                projectDirectory = project.toExistingDirectory() ?: return@EnvironmentSelectionWindow,
                                environmentsDirectoryPath = environmentsDirectoryPath.toNonEmptyString()
                                    ?: return@EnvironmentSelectionWindow,
                                newSelectedEnvironment = newSelectedEnvironment?.toNonEmptyString()
                            )
                        },
                    )
                }
            }
        }
    }

    private fun collectState(project: Project) {
        uiScope.launch {
            presenter.stateFlow.collect { environmentSelectionState ->
                val projectDirectory = project.toExistingDirectory() ?: return@collect
                environmentSelectionWindowState.value =
                    environmentSelectionState.toEnvironmentsSelectionWindowState(projectDirectory.path.value)
            }
        }
    }

    private fun Project.scanProject() {
        val projectDirectory = toExistingDirectory() ?: return
        presenter.dispatch(EnvironmentSelectionAction.ScanProject(projectDirectory))
    }

    private fun Project.onSelectedEnvironmentChanged(
        projectDirectory: ExistingDirectory,
        environmentsDirectoryPath: NonEmptyString,
        newSelectedEnvironment: NonEmptyString?
    ) {
        presenter.dispatch(
            EnvironmentSelectionAction.SelectEnvironment(
                projectDirectory = projectDirectory,
                environmentsDirectoryPath = environmentsDirectoryPath,
                newSelectedEnvironment = newSelectedEnvironment,
            )
        )
    }

    override fun dispose() {
        uiScope.cancel()
        ioScope.cancel()
    }
}