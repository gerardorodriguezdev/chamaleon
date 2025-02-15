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
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.CreateEnvironmentPresenter
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings.BundleStringsProvider
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings.BundleStringsProvider.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.theme.PluginTheme.Theme
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.Action.ExternalAction.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindow
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State
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
    private val scope = CoroutineScope(Dispatchers.Swing)

    private val presenter = CreateEnvironmentPresenter(
        projectDirectory = projectDirectory,
        stringsProvider = BundleStringsProvider,
        environmentsProcessor = environmentsProcessor,
        uiDispatcher = Dispatchers.Swing,
        ioDispatcher = Dispatchers.IO,
        onSelectEnvironmentPathClicked = { selectFileDirectory(project) }
    )

    private val state = mutableStateOf<State>(
        State.SetupEnvironmentState(path = "", verification = null)
    )

    init {
        collectState()
    }

    @OptIn(ExperimentalComposeUiApi::class, ExperimentalJewelApi::class)
    override fun createCenterPanel(): JComponent {
        enableNewSwingCompositing()

        return JewelComposePanel {
            with(LocalDensity.current) {
                Theme {
                    CreateEnvironmentWindow(
                        state = state.value,
                        onAction = presenter::onAction,
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
        scope.launch {
            presenter.state.collect { createEnvironmentState ->
                state.value = createEnvironmentState.toState()

                setDialogButtonsState(
                    isPreviousButtonEnabled = createEnvironmentState.isPreviousButtonEnabled,
                    isNextButtonEnabled = createEnvironmentState.isNextButtonEnabled,
                    isFinishButtonEnabled = createEnvironmentState.isFinishButtonEnabled,
                )
            }
        }
    }

    //TODO: Add all steps
    private fun CreateEnvironmentPresenter.CreateEnvironmentState.toState(): State =
        when (step) {
            CreateEnvironmentPresenter.CreateEnvironmentState.Step.SETUP_ENVIRONMENT -> {
                State.SetupEnvironmentState(
                    path = environmentsDirectoryPath ?: "",
                    verification = verification,
                )
            }
        }

    //TODO: Move this types of actions
    override fun onDialogAction(action: DialogAction) {
        when (action) {
            is DialogAction.OnPreviousButtonClicked -> presenter.onAction(OnPreviousButtonClicked)
            is DialogAction.OnNextButtonClicked -> presenter.onAction(OnNextButtonClicked)
            is DialogAction.OnFinishButtonClicked -> presenter.onAction(OnFinishButtonClicked)
        }
    }

    override fun dispose() {
        scope.cancel()
        presenter.dispose()
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