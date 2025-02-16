package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter

import com.intellij.openapi.Disposable
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import kotlin.coroutines.CoroutineContext

internal class CreateEnvironmentPresenter(
    private val projectDirectory: File,

    private val stringsProvider: StringsProvider,
    private val environmentsProcessor: EnvironmentsProcessor,

    private val uiDispatcher: CoroutineContext,
    ioDispatcher: CoroutineContext,

    private val onSelectEnvironmentPathClicked: () -> String?,
) : Disposable {
    private val mutableState = MutableStateFlow<CreateEnvironmentState>(CreateEnvironmentState())
    val state: StateFlow<CreateEnvironmentState> = mutableState

    private val ioScope = CoroutineScope(ioDispatcher)

    fun onAction(action: CreateEnvironmentAction) {

    }

    override fun dispose() {
        ioScope.cancel()
    }
}