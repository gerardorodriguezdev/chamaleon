package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.toolWindows.environmentSelectionToolWindow

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

    init {
        collectState()
    }

    @OptIn(ExperimentalJewelApi::class)
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
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

    private fun collectState() {
        uiScope.launch {
            presenter.stateFlow.collect { environmentSelectionState ->
                environmentSelectionWindowState.value = environmentSelectionState.toEnvironmentsSelectionWindowState()
            }
        }
    }

    private fun Project.scanProject() {
        val projectDirectoryPath = basePath ?: return
        val projectDirectory = projectDirectoryPath.toExistingDirectory() ?: return
        presenter.dispatch(EnvironmentSelectionAction.ScanProject(projectDirectory))
    }

    private fun Project.onSelectedEnvironmentChanged(
        environmentsDirectoryPath: NonEmptyString,
        newSelectedEnvironment: NonEmptyString?
    ) {
        presenter.dispatch(
            EnvironmentSelectionAction.SelectEnvironment(
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