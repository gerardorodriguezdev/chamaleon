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
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory.Companion.toExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectDeserializer
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectSerializer
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.BaseDialog
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.createProjectDialog.mappers.toCreateProjectAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.createProjectDialog.mappers.toCreateProjectWindowState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.createProjectDialog.mappers.toDialogButtonsState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.CreateProjectAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.CreateProjectPresenter
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.CreateProjectState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings.BundleStringsProvider
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

//TODO: Fix
internal class CreateProjectDialog(
    private val project: Project,
    projectDirectory: ExistingDirectory,
    private val projectDeserializer: ProjectDeserializer,
) : BaseDialog(dialogTitle = BundleStringsProvider.string(StringsKeys.createEnvironment)) {
    private val uiScope = CoroutineScope(Dispatchers.Swing)
    private val ioScope = CoroutineScope(Dispatchers.IO)

    private val presenter = CreateProjectPresenter(
        uiScope = uiScope,
        ioScope = ioScope,
        projectDeserializer = projectDeserializer,
        stringsProvider = BundleStringsProvider,
    )

    private val createProjectWindowState =
        mutableStateOf<CreateProjectWindowState>(CreateProjectWindowState.SetupEnvironmentState())

    init {
        collectState()

        presenter.dispatch(CreateProjectAction.SetupEnvironmentAction.OnEnvironmentsDirectoryChanged(projectDirectory))
    }

    private fun collectState() {
        uiScope.launch {
            presenter.stateFlow.collect { createProjectState ->
                setDialogButtonsState(createProjectState.toDialogButtonsState())
                if (createProjectState is CreateProjectState.Finish) {
                    project.generateEnvironments(createProjectState.project)
                }

                val projectDirectory = project.toExistingDirectory() ?: return@collect
                val newCreateProjectWindowState =
                    createProjectState.toCreateProjectWindowState(projectDirectory.path.value, BundleStringsProvider)
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
                        onAction = { action ->
                            //TODO: Refactor a bit
                            when (action) {
                                is CreateProjectWindowAction.SetupEnvironmentAction.OnSelectEnvironmentPath -> {
                                    val newSelectedEnvironmentDirectory =
                                        project.selectFileDirectoryPath()?.toExistingDirectory()
                                    newSelectedEnvironmentDirectory?.let {
                                        presenter.dispatch(
                                            CreateProjectAction.SetupEnvironmentAction.OnEnvironmentsDirectoryChanged(
                                                newSelectedEnvironmentDirectory
                                            )
                                        )
                                    }
                                }

                                else -> {
                                    val action = action.toCreateProjectAction()
                                    action?.let {
                                        presenter.dispatch(action)
                                    }
                                }
                            }
                        },
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
            taskName = BundleStringsProvider.string(StringsKeys.generateEnvironment),
            task = { indicator ->
                val projectSerializer = ProjectSerializer.Companion.create()
                val projectSerializationResult = projectSerializer.serialize(chamaleonProject)
                when (projectSerializationResult) {
                    is ProjectSerializationResult.Success -> {
                        showSuccessNotification(title = "", message = "") //TODO: Lexemes
                        chamaleonProject.environmentsDirectory.notifyDirectoryChanged()
                        // TODO: Notify plugin to update files as well
                    }

                    is ProjectSerializationResult.Failure -> showFailureNotification(
                        title = "",
                        message = ""
                    ) //TODO: Lexemes
                }
            }
        )
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