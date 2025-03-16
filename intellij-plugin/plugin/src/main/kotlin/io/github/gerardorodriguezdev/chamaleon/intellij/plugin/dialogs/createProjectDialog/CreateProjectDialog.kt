package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.createProjectDialog

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.toSize
import com.intellij.openapi.project.Project
import com.intellij.ui.util.minimumHeight
import com.intellij.ui.util.minimumWidth
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectSerializationResult
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString.Companion.toNonEmptyString
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectDeserializer
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectSerializer
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.BaseDialog
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.createProjectDialog.mappers.toCreateProjectAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.createProjectDialog.mappers.toCreateProjectWindowState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.createProjectDialog.mappers.toDialogButtonsState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.createProjectDialog.mappers.toErrorMessage
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.CreateProjectAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.CreateProjectPresenter
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings.BundleStringsProvider
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings.BundleStringsProvider.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.theme.PluginTheme.Theme
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindow
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import org.jetbrains.jewel.bridge.JewelComposePanel
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.enableNewSwingCompositing
import javax.swing.JComponent
import io.github.gerardorodriguezdev.chamaleon.core.models.Project as ChamaleonProject

//TODO: Function calls with named params (grad plug + intell mods)
//TODO: Notify plugin to update files as well
internal class CreateProjectDialog(
    private val project: Project,
    private val projectDirectory: ExistingDirectory,
    private val projectDeserializer: ProjectDeserializer,
) : BaseDialog(dialogTitle = string(StringsKeys.createEnvironment)) {
    private val uiScope = CoroutineScope(Dispatchers.Swing)
    private val ioScope = CoroutineScope(Dispatchers.IO)

    private val presenter = CreateProjectPresenter(
        uiScope = uiScope,
        ioScope = ioScope,
        projectDeserializer = projectDeserializer,
        stringsProvider = BundleStringsProvider,
        onFinish = { chamaleonProject ->
            project.generateEnvironments(chamaleonProject)
        }
    )

    private val createProjectWindowState =
        mutableStateOf<CreateProjectWindowState>(CreateProjectWindowState.SetupEnvironmentState())

    init {
        collectState()

        presenter.dispatch(
            CreateProjectAction.SetupEnvironmentAction.OnEnvironmentsDirectoryPathChanged(projectDirectory.path)
        )
    }

    private fun collectState() {
        uiScope.launch {
            presenter.stateFlow.collect { createProjectState ->
                setDialogButtonsState(createProjectState.toDialogButtonsState())

                val newCreateProjectWindowState =
                    createProjectState.toCreateProjectWindowState(
                        projectDirectoryPath = projectDirectory.path.value,
                        stringsProvider = BundleStringsProvider,
                    )
                newCreateProjectWindowState?.let {
                    createProjectWindowState.value = newCreateProjectWindowState
                }
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class, ExperimentalJewelApi::class)
    override fun createCenterPanel(): JComponent {
        enableNewSwingCompositing()

        return JewelComposePanel {
            with(LocalDensity.current) {
                Theme {
                    CreateProjectWindow(
                        state = createProjectWindowState.value,
                        onAction = { action -> action.handle() },
                        modifier = Modifier.Companion.requiredSize(
                            LocalWindowInfo.current.containerSize.toSize().toDpSize()
                        )
                    )
                }
            }
        }.apply {
            minimumWidth = DIALOG_MIN_SIZE
            minimumHeight = DIALOG_MIN_SIZE
        }
    }

    override fun onDialogAction(action: DialogAction) {
        presenter.dispatch(action.toCreateProjectAction())
    }

    private fun Project.generateEnvironments(chamaleonProject: ChamaleonProject) {
        runBackgroundTask(
            taskName = string(StringsKeys.generateEnvironment),
            task = {
                val projectSerializer = ProjectSerializer.Companion.create()
                val projectSerializationResult = projectSerializer.serialize(chamaleonProject)
                reportProjectSerializationResult(projectSerializationResult, chamaleonProject)
            }
        )
    }

    private suspend fun Project.reportProjectSerializationResult(
        projectSerializationResult: ProjectSerializationResult,
        chamaleonProject: ChamaleonProject,
    ) {
        //TODO: Do in correct coroutine
        when (projectSerializationResult) {
            is ProjectSerializationResult.Success -> {
                showSuccessNotification(
                    title = string(StringsKeys.chamaleonEnvironmentGeneration),
                    message = string(StringsKeys.environmentGeneratedSuccessfully)
                )
                chamaleonProject.environmentsDirectory.notifyDirectoryChanged()
            }

            is ProjectSerializationResult.Failure ->
                showFailureNotification(
                    title = string(StringsKeys.chamaleonEnvironmentGeneration),
                    message = projectSerializationResult.toErrorMessage(BundleStringsProvider)
                )
        }
    }

    private fun CreateProjectWindowAction.handle() {
        when (this) {
            is CreateProjectWindowAction.SetupEnvironmentAction.OnSelectEnvironmentPath -> handle()
            else -> {
                val action = toCreateProjectAction()
                action?.let {
                    presenter.dispatch(action)
                }
            }
        }
    }

    private fun CreateProjectWindowAction.SetupEnvironmentAction.OnSelectEnvironmentPath.handle() {
        val newSelectedEnvironmentDirectory = project.selectFileDirectoryPath()?.toNonEmptyString()
        newSelectedEnvironmentDirectory?.let {
            presenter.dispatch(
                CreateProjectAction.SetupEnvironmentAction.OnEnvironmentsDirectoryPathChanged(
                    newSelectedEnvironmentDirectory
                )
            )
        }
    }

    override fun dispose() {
        uiScope.cancel()
        ioScope.cancel()
        super.dispose()
    }

    private companion object {
        const val DIALOG_MIN_SIZE = 400
    }
}