package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.plugin

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.intellij.openapi.ui.DialogWrapper
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.plugin.messages.Bundle
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.plugin.presenters.CreateEnvironmentPresenter
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.plugin.ui.windows.createEnvironment.CreateEnvironmentWindow
import org.jetbrains.jewel.bridge.JewelComposePanel
import java.awt.event.ActionEvent
import javax.swing.Action
import javax.swing.JButton
import javax.swing.JComponent

//TODO: Finish
internal class EnvironmentCreationDialog : DialogWrapper(false) {
    private val presenter = CreateEnvironmentPresenter(onSelectEnvironmentPathClicked = {})

    private val cancel = Cancel()
    private val previous = Previous()
    private val next = Next()
    private val finish = Finish()

    init {
        title = Bundle.createEnvironment

        init()
    }

    override fun createCenterPanel(): JComponent =
        JewelComposePanel {
            val state by presenter.state.collectAsState()
            CreateEnvironmentWindow(state = state)
        }

    override fun createActions(): Array<Action> {
        return arrayOf(
            cancel,
            previous,
            next,
            finish,
        )
    }

    private fun cancelButton(): JButton? = getButton(cancel)

    private fun previousButton(): JButton? = getButton(previous)

    private fun nextButton(): JButton? = getButton(next)

    private fun finishButton(): JButton? = getButton(finish)

    private inner class Cancel : DialogWrapperAction(Bundle.cancel) {
        init {
            putValue(MAC_ACTION_ORDER, CANCEL_ACTION_ORDER)
        }

        override fun doAction(p0: ActionEvent?) {
            close(CANCEL_EXIT_CODE)
        }
    }

    private inner class Previous : DialogWrapperAction(Bundle.previous) {
        init {
            putValue(MAC_ACTION_ORDER, PREVIOUS_ACTION_ORDER)
        }

        override fun doAction(p0: ActionEvent?) {
            //TODO: Do
        }
    }

    private inner class Next : DialogWrapperAction(Bundle.next) {
        init {
            putValue(DEFAULT_ACTION, true)
            putValue(MAC_ACTION_ORDER, NEXT_ACTION_ORDER)
        }

        override fun doAction(p0: ActionEvent?) {
            // TODO: Do
        }
    }

    private inner class Finish : DialogWrapperAction(Bundle.finish) {
        init {
            putValue(DEFAULT_ACTION, true)
            putValue(MAC_ACTION_ORDER, DEFAULT_ACTION_ORDER)
        }

        override fun doAction(p0: ActionEvent?) {
            close(OK_EXIT_CODE)
        }
    }

    companion object {
        const val CANCEL_ACTION_ORDER = -10
        const val PREVIOUS_ACTION_ORDER = -9
        const val NEXT_ACTION_ORDER = 90
    }
}