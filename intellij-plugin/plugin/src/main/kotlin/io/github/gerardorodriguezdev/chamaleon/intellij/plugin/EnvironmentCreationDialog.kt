package io.github.gerardorodriguezdev.chamaleon.intellij.plugin

import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.intellij.openapi.application.EDT
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
import javax.swing.JComponent

internal class EnvironmentCreationDialog : BaseDialog(dialogTitle = string(StringsKeys.createEnvironment)) {
    private val scope = CoroutineScope(Dispatchers.EDT)

    private val presenter = CreateEnvironmentPresenter(
        uiDispatcher = Dispatchers.EDT,
        ioDispatcher = Dispatchers.IO,
        onSelectEnvironmentPathClicked = {
            //TODO: Finish
        }
    )

    init {
        collectState()
    }

    override fun createCenterPanel(): JComponent =
        JewelComposePanel {
            Theme {
                val state by presenter.state.collectAsState()
                CreateEnvironmentWindow(
                    state = state,
                    onAction = presenter::onAction,
                    modifier = Modifier.sizeIn(maxWidth = 300.dp, maxHeight = 300.dp)
                )
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
}