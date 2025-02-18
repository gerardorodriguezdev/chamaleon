package io.github.gerardorodriguezdev.chamaleon.intellij.plugin

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.toSize
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.ui.util.minimumHeight
import com.intellij.ui.util.minimumWidth
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.BaseDialog
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentPresenter
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.handlers.DefaultSetupEnvironmentHandler
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.mappers.toCreateEnvironmentAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.mappers.toDialogButtonsState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.mappers.toWindowState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings.BundleStringsProvider.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.theme.PluginTheme.Theme
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindow
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowState.SetupEnvironmentState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import org.jetbrains.jewel.bridge.JewelComposePanel
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.enableNewSwingCompositing
import java.io.File
import javax.swing.JComponent

internal class EnvironmentCreationDialog(
    project: Project,
    projectDirectory: File,
    environmentsProcessor: EnvironmentsProcessor,
) : BaseDialog(dialogTitle = string(StringsKeys.createEnvironment)) {
    private val uiScope = CoroutineScope(Dispatchers.Swing)

    private val presenter = CreateEnvironmentPresenter(
        uiScope = uiScope,
        ioContext = Dispatchers.IO,
        projectDirectory = projectDirectory,
        setupEnvironmentHandler = DefaultSetupEnvironmentHandler(environmentsProcessor),
        onEnvironmentsDirectorySelected = { selectFileDirectory(project) }
    )

    private val createEnvironmentWindowState = mutableStateOf<CreateEnvironmentWindowState>(SetupEnvironmentState())

    init {
        collectState()

        presenter.onAction(CreateEnvironmentAction.SetupEnvironmentAction.OnInit)
    }

    @OptIn(ExperimentalComposeUiApi::class, ExperimentalJewelApi::class)
    override fun createCenterPanel(): JComponent {
        enableNewSwingCompositing()

        return JewelComposePanel {
            with(LocalDensity.current) {
                Theme {
                    CreateEnvironmentWindow(
                        state = createEnvironmentWindowState.value,
                        onAction = { action ->
                            presenter.onAction(action.toCreateEnvironmentAction())
                        },
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
                createEnvironmentWindowState.value = createEnvironmentState.toWindowState()

                setDialogButtonsState(createEnvironmentState.toDialogButtonsState())
            }
        }
    }

    override fun onDialogAction(action: DialogAction) {
        presenter.onAction(action.toCreateEnvironmentAction())
    }

    override fun dispose() {
        uiScope.cancel()
        super.dispose()
    }

    private fun selectFileDirectory(project: Project): String? {
        val fileDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
        val selectedDirectory = FileChooser.chooseFile(
            fileDescriptor,
            project,
            null
        )
        return selectedDirectory?.path
    }

    private companion object {
        const val DIALOG_MIN_SIZE = 400
    }
}