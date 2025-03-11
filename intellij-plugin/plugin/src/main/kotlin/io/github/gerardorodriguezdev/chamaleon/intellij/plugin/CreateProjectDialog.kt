package io.github.gerardorodriguezdev.chamaleon.intellij.plugin

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
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectDeserializer
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectSerializer
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.BaseDialog
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.CreateProjectAction.SetupEnvironmentAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.CreateProjectPresenter
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings.BundleStringsProvider
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings.BundleStringsProvider.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.theme.PluginTheme.Theme
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindow
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowState.SetupEnvironmentState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.utils.notifyDirectoryChanged
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.utils.runBackgroundTask
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.utils.showFailureNotification
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.utils.showSuccessNotification
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

internal class CreateProjectDialog(
    project: Project,
    projectDirectory: ExistingDirectory,
    private val projectDeserializer: ProjectDeserializer,
) : BaseDialog(dialogTitle = string(StringsKeys.createEnvironment)) {
    private val uiScope = CoroutineScope(Dispatchers.Swing)
    private val ioScope = CoroutineScope(Dispatchers.IO)

    private val presenter = CreateProjectPresenter(
        uiScope = uiScope,
        ioScope = ioScope,
        projectDeserializer = projectDeserializer,
        onFinish = { chamaleonProject -> project.generateEnvironments(chamaleonProject) }
    )

    private val createProjectWindowState = mutableStateOf<CreateProjectWindowState>(SetupEnvironmentState())

    init {
        collectState()

        presenter.dispatch(SetupEnvironmentAction.OnEnvironmentsDirectoryChanged(projectDirectory))
    }

    @OptIn(ExperimentalComposeUiApi::class, ExperimentalJewelApi::class)
    override fun createCenterPanel(): JComponent {
        enableNewSwingCompositing()

        return JewelComposePanel {
            with(LocalDensity.current) {
                Theme {
                    CreateProjectWindow(
                        state = createProjectWindowState.value,
                        onAction = { action -> presenter.dispatch(action.toCreateEnvironmentAction()) },
                        modifier = Modifier.requiredSize(LocalWindowInfo.current.containerSize.toSize().toDpSize())
                    )
                }
            }
        }.apply {
            minimumWidth = DIALOG_MIN_SIZE
            minimumHeight = DIALOG_MIN_SIZE
        }
    }

    private fun collectState() {
        uiScope.launch {
            presenter.stateFlow.collect { createEnvironmentState ->
                createProjectWindowState.value = createEnvironmentState.toWindowState(BundleStringsProvider)
                setDialogButtonsState(createEnvironmentState.toDialogButtonsState())
            }
        }
    }

    override fun onDialogAction(action: DialogAction) {
        presenter.dispatch(action.toCreateEnvironmentAction())
    }

    override fun dispose() {
        uiScope.cancel()
        ioScope.cancel()
        super.dispose()
    }

    private fun Project.generateEnvironments(chamaleonProject: ChamaleonProject) {
        runBackgroundTask(
            taskName = string(StringsKeys.generateEnvironment),
            task = { indicator ->
                val projectSerializer = ProjectSerializer.create()
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

    private companion object {
        const val DIALOG_MIN_SIZE = 400
    }
}