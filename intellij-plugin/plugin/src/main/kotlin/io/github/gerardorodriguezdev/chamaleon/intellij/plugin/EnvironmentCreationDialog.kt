package io.github.gerardorodriguezdev.chamaleon.intellij.plugin

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.toSize
import com.intellij.openapi.application.EDT
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.ui.util.minimumHeight
import com.intellij.ui.util.minimumWidth
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.BaseDialog
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.CreateEnvironmentPresenter
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings.BundleStringsProvider.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.theme.PluginTheme.Theme
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.Action.ExternalAction.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.jetbrains.jewel.bridge.JewelComposePanel
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.enableNewSwingCompositing
import java.io.File
import javax.swing.JComponent

internal class EnvironmentCreationDialog(
    project: Project,
    environmentsProcessor: EnvironmentsProcessor,
) : BaseDialog(dialogTitle = string(StringsKeys.createEnvironment)) {
    private val scope = CoroutineScope(Dispatchers.EDT)

    private val presenter = CreateEnvironmentPresenter(
        rootProjectFile = project.projectFile?.path?.toFileOrNull(),
        environmentsProcessor = environmentsProcessor,
        uiDispatcher = Dispatchers.EDT,
        ioDispatcher = Dispatchers.IO,
        onSelectEnvironmentPathClicked = { selectFileDirectory(project) }
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
                    val state by presenter.state.collectAsState()
                    CreateEnvironmentWindow(
                        state = state,
                        onAction = presenter::onAction,
                        modifier = Modifier.requiredSize(LocalWindowInfo.current.containerSize.toSize().toDpSize())
                    )
                }
            }
        }.apply {
            minimumWidth = 400
            minimumHeight = 400
        }
    }

    override fun onDialogAction(action: DialogAction) {
        when (action) {
            is DialogAction.OnPreviousButtonClicked -> presenter.onAction(OnPreviousButtonClicked)
            is DialogAction.OnNextButtonClicked -> presenter.onAction(OnNextButtonClicked)
            is DialogAction.OnFinishButtonClicked -> presenter.onAction(OnFinishButtonClicked)
        }
    }

    private fun collectState() {
        scope.launch {
            presenter.state.collect { state ->
                setDialogButtonsState(
                    isPreviousButtonEnabled = state.isPreviousButtonEnabled,
                    isNextButtonEnabled = state.isNextButtonEnabled,
                    isFinishButtonEnabled = state.isFinishButtonEnabled,
                )
            }
        }
    }

    override fun dispose() {
        scope.cancel()
        presenter.dispose()
        super.dispose()
    }

    private fun String?.toFileOrNull(): File? = if (this == null) null else File(this)

    private fun selectFileDirectory(project: Project): String? {
        val fileDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
        val selectedDirectory = FileChooser.chooseFile(
            fileDescriptor,
            project,
            null
        )
        return selectedDirectory?.path
    }
}