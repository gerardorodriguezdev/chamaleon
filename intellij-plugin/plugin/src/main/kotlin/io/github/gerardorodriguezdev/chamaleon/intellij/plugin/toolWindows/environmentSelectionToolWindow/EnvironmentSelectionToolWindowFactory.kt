package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.toolWindows.environmentSelectionToolWindow

import androidx.compose.runtime.mutableStateOf
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import io.github.gerardorodriguezdev.chamaleon.core.Versions
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectSerializationResult
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString.Companion.toNonEmptyString
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectDeserializer
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectSerializer
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.createProjectDialog.CreateProjectDialog
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.createProjectDialog.mappers.toErrorMessage
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.environmentSelectionPresenter.EnvironmentSelectionAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.environmentSelectionPresenter.EnvironmentSelectionPresenter
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.toolWindows.environmentSelectionToolWindow.mappers.toEnvironmentsSelectionWindowState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings.BundleStringsProvider
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings.BundleStringsProvider.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.theme.PluginTheme.Theme
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.theme.PluginTheme.stringsProvider
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.EnvironmentSelectionWindow
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.EnvironmentSelectionWindowState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import org.jetbrains.jewel.bridge.addComposeTab
import org.jetbrains.jewel.bridge.theme.SwingBridgeTheme
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import io.github.gerardorodriguezdev.chamaleon.core.models.Project as ChamaleonProject

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

        toolWindow.addComposeTab(tabDisplayName = string(StringsKeys.environmentSelectionWindowName)) {
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
                                onFinish = { chamaleonProject ->
                                    project.createProject(chamaleonProject)
                                },
                            ).show()
                        },
                        onSelectedEnvironmentChanged = { index, newSelectedEnvironment ->
                            project.onSelectedEnvironmentChanged(
                                index = index,
                                newSelectedEnvironment = newSelectedEnvironment?.toNonEmptyString()
                            )
                        },
                        onSelectEnvironment = { index ->
                            val chamaleonProject = presenter.stateFlow.value.projects?.values?.elementAtOrNull(index)
                            val environmentsDirectory =
                                chamaleonProject?.environmentsDirectory ?: return@EnvironmentSelectionWindow
                            project.openDirectory(environmentsDirectory)
                        }
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
        index: Int,
        newSelectedEnvironment: NonEmptyString?
    ) {
        presenter.dispatch(
            EnvironmentSelectionAction.SelectEnvironment(
                index = index,
                newSelectedEnvironment = newSelectedEnvironment,
            )
        )
    }

    private fun Project.createProject(chamaleonProject: ChamaleonProject) {
        ioScope.launch {
            val projectSerializer = ProjectSerializer.create()
            val projectSerializationResult = projectSerializer.serialize(chamaleonProject)

            withContext(Dispatchers.EDT) {
                reportProjectSerializationResult(projectSerializationResult, chamaleonProject)
            }
        }
    }

    private fun Project.reportProjectSerializationResult(
        projectSerializationResult: ProjectSerializationResult,
        chamaleonProject: ChamaleonProject,
    ) {
        when (projectSerializationResult) {
            is ProjectSerializationResult.Success -> {
                chamaleonProject.environmentsDirectory.notifyDirectoryChanged()

                scanProject()

                openDirectory(chamaleonProject.environmentsDirectory)

                showSuccessNotification(
                    title = string(StringsKeys.chamaleonEnvironmentGeneration),
                    message = string(StringsKeys.environmentGeneratedSuccessfully)
                )
            }

            is ProjectSerializationResult.Failure ->
                showFailureNotification(
                    title = string(StringsKeys.chamaleonEnvironmentGeneration),
                    message = projectSerializationResult.toErrorMessage(BundleStringsProvider)
                )
        }
    }

    override fun dispose() {
        uiScope.cancel()
        ioScope.cancel()
    }
}