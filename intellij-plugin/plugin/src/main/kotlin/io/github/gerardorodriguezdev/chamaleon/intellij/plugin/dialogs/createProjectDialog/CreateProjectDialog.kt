package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.createProjectDialog

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.toSize
import com.intellij.openapi.project.Project
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString.Companion.toNonEmptyString
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectDeserializer
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.BaseDialog
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.createProjectDialog.mappers.toCreateProjectAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.createProjectDialog.mappers.toCreateProjectWindowState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.createProjectDialog.mappers.toDialogButtonsState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.CreateProjectAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.CreateProjectPresenter
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings.BundleStringsProvider
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings.BundleStringsProvider.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.theme.PluginTheme.Theme
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindow
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.utils.selectFileDirectoryPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import org.jetbrains.jewel.bridge.JewelComposePanel
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.enableNewSwingCompositing
import java.awt.Dimension
import javax.swing.JComponent
import io.github.gerardorodriguezdev.chamaleon.core.models.Project as ChamaleonProject

internal class CreateProjectDialog(
    private val project: Project,
    private val projectDirectory: ExistingDirectory,
    private val projectDeserializer: ProjectDeserializer,
    private val onFinish: (project: ChamaleonProject) -> Unit,
) : BaseDialog(dialogTitle = string(StringsKeys.createEnvironment)) {
    private val uiScope = CoroutineScope(Dispatchers.Swing)
    private val ioScope = CoroutineScope(Dispatchers.IO)

    private val presenter = CreateProjectPresenter(
        uiScope = uiScope,
        ioScope = ioScope,
        projectDeserializer = projectDeserializer,
        stringsProvider = BundleStringsProvider,
        onFinish = onFinish,
    )

    private val createProjectWindowState =
        mutableStateOf<CreateProjectWindowState>(CreateProjectWindowState.SetupEnvironmentState())

    init {
        collectState()

        presenter.dispatch(
            CreateProjectAction.SetupEnvironmentAction.OnEnvironmentsDirectoryPathChanged(projectDirectory.path)
        )
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
                        modifier = Modifier.requiredSize(
                            LocalWindowInfo.current.containerSize.toSize().toDpSize()
                        )
                    )
                }
            }
        }.apply {
            minimumSize = Dimension(DIALOG_MIN_SIZE, DIALOG_MIN_SIZE)
        }
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

    override fun onDialogAction(action: DialogAction) {
        presenter.dispatch(action.toCreateProjectAction())
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