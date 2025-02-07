package io.github.gerardorodriguezdev.chamaleon.intellij.plugin

import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.intellij.openapi.application.EDT
import com.intellij.openapi.ui.DialogWrapper
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.CreateEnvironmentPresenter
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings.BundleStringsProvider.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.theme.PluginTheme.Theme
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.Action.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindow
import kotlinx.coroutines.*
import org.jetbrains.jewel.bridge.JewelComposePanel
import java.awt.event.ActionEvent
import javax.swing.Action
import javax.swing.JButton
import javax.swing.JComponent

internal class EnvironmentCreationDialog : DialogWrapper(false) {
    private val scope = CoroutineScope(Dispatchers.EDT + SupervisorJob())

    private val presenter = CreateEnvironmentPresenter(
        onSelectEnvironmentPathClicked = {
            //TODO: Finish
        }
    )

    private val cancel = Cancel()
    private val previous = Previous()
    private val next = Next()
    private val finish = Finish()

    init {
        title = string(StringsKeys.createEnvironment)

        init()

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

    override fun createActions(): Array<Action> {
        return arrayOf(
            cancel,
            previous,
            next,
            finish,
        )
    }

    private fun collectState() {
        scope.launch {
            presenter.state.collect { state ->
                previousButton()?.isEnabled = state.isPreviousButtonEnabled
                nextButton()?.isEnabled = state.isNextButtonEnabled
                finishButton()?.isEnabled = state.isFinishButtonEnabled
            }
        }
    }

    override fun dispose() {
        scope.cancel()
        presenter.dispose()
        super.dispose()
    }

    private fun previousButton(): JButton? = getButton(previous)

    private fun nextButton(): JButton? = getButton(next)

    private fun finishButton(): JButton? = getButton(finish)

    private inner class Cancel : DialogWrapperAction(string(StringsKeys.cancel)) {
        init {
            putValue(MAC_ACTION_ORDER, CANCEL_ACTION_ORDER)
        }

        override fun doAction(p0: ActionEvent?) {
            close(CANCEL_EXIT_CODE)
        }
    }

    private inner class Previous : DialogWrapperAction(string(StringsKeys.previous)) {
        init {
            putValue(MAC_ACTION_ORDER, PREVIOUS_ACTION_ORDER)
        }

        override fun doAction(p0: ActionEvent?) {
            presenter.onAction(OnPreviousButtonClicked)
        }
    }

    private inner class Next : DialogWrapperAction(string(StringsKeys.next)) {
        init {
            putValue(DEFAULT_ACTION, true)
            putValue(MAC_ACTION_ORDER, NEXT_ACTION_ORDER)
        }

        override fun doAction(p0: ActionEvent?) {
            presenter.onAction(OnNextButtonClicked)
        }
    }

    private inner class Finish : DialogWrapperAction(string(StringsKeys.finish)) {
        init {
            putValue(DEFAULT_ACTION, true)
            putValue(MAC_ACTION_ORDER, DEFAULT_ACTION_ORDER)
        }

        override fun doAction(p0: ActionEvent?) {
            presenter.onAction(OnFinishButtonClicked)
            close(OK_EXIT_CODE)
        }
    }

    companion object {
        const val CANCEL_ACTION_ORDER = -10
        const val PREVIOUS_ACTION_ORDER = -9
        const val NEXT_ACTION_ORDER = 90
    }
}