package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs

import com.intellij.openapi.ui.DialogWrapper
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.BaseDialog.DialogAction.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings.BundleStringsProvider.string
import java.awt.event.ActionEvent
import javax.swing.Action
import javax.swing.JButton

internal abstract class BaseDialog(dialogTitle: String) : DialogWrapper(false) {
    private val cancel = Cancel()
    private val previous = Previous()
    private val next = Next()
    private val finish = Finish()

    init {
        title = dialogTitle

        init()
    }

    abstract fun onDialogAction(action: DialogAction)

    protected fun setDialogButtonsState(dialogsButtonsState: DialogButtonsState) {
        previousButton()?.isEnabled = dialogsButtonsState.isPreviousButtonEnabled
        nextButton()?.isEnabled = dialogsButtonsState.isNextButtonEnabled
        finishButton()?.isEnabled = dialogsButtonsState.isFinishButtonEnabled
    }

    override fun createActions(): Array<Action> =
        arrayOf(
            cancel,
            previous,
            next,
            finish,
        )

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
            onDialogAction(OnPreviousButtonClicked)
        }
    }

    private inner class Next : DialogWrapperAction(string(StringsKeys.next)) {
        init {
            putValue(DEFAULT_ACTION, true)
            putValue(MAC_ACTION_ORDER, NEXT_ACTION_ORDER)
        }

        override fun doAction(p0: ActionEvent?) {
            onDialogAction(OnNextButtonClicked)
        }
    }

    private inner class Finish : DialogWrapperAction(string(StringsKeys.finish)) {
        init {
            putValue(DEFAULT_ACTION, true)
            putValue(MAC_ACTION_ORDER, DEFAULT_ACTION_ORDER)
        }

        override fun doAction(p0: ActionEvent?) {
            onDialogAction(OnFinishButtonClicked)
            close(OK_EXIT_CODE)
        }
    }

    data class DialogButtonsState(
        val isPreviousButtonEnabled: Boolean,
        val isNextButtonEnabled: Boolean,
        val isFinishButtonEnabled: Boolean,
    )

    sealed interface DialogAction {
        data object OnPreviousButtonClicked : DialogAction
        data object OnNextButtonClicked : DialogAction
        data object OnFinishButtonClicked : DialogAction
    }

    companion object {
        const val CANCEL_ACTION_ORDER = -10
        const val PREVIOUS_ACTION_ORDER = -9
        const val NEXT_ACTION_ORDER = 90
    }
}