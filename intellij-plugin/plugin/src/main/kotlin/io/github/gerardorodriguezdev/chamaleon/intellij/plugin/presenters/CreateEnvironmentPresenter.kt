package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters

import com.intellij.openapi.Disposable
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.EnvironmentsProcessorResult
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.SchemaParserResult
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.Action
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.Action.SetupEnvironmentAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.LoadingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.CoroutineContext

// TODO: Update path selected here
// TODO: Try processing env
// TODO: Title for setup schema depending if schema existing or not
// TODO: Execute externally notification of progress
internal class CreateEnvironmentPresenter(
    private val rootProjectFile: File?,
    private val environmentsProcessor: EnvironmentsProcessor,
    private val uiDispatcher: CoroutineContext,
    ioDispatcher: CoroutineContext,
    private val onSelectEnvironmentPathClicked: () -> String?,
) : Disposable {
    private val _state = MutableStateFlow<State>(value = LoadingState())
    val state: StateFlow<State> = _state

    private val ioScope = CoroutineScope(ioDispatcher)

    private var environmentsProcessorResult: EnvironmentsProcessorResult? = null

    init {
        onInit()
    }

    private fun onInit() {
        val rootProjectFile = rootProjectFile
        if (rootProjectFile == null) {
            _state.value = State.SetupEnvironmentState(path = "", verification = null)
            return
        }

        ioScope.launch {
            val result = environmentsProcessor.process(rootProjectFile)

            withContext(uiDispatcher) {
                when (result) {
                    is EnvironmentsProcessorResult.Success -> {
                        environmentsProcessorResult = result
                    }

                    is EnvironmentsProcessorResult.Failure -> when (result) {
                        is EnvironmentsProcessorResult.Failure.EnvironmentsDirectoryNotFound -> {}

                        is EnvironmentsProcessorResult.Failure.SchemaParsingError ->
                            when (result.schemaParsingError) {
                                is SchemaParserResult.Failure.FileNotFound -> {}
                                else -> {}
                            }

                        else -> {}
                    }
                }
            }
        }
    }

    fun onAction(action: Action) {
        when (action) {
            is Action.ExternalAction -> Unit
            is SetupEnvironmentAction -> action.handle()
            is Action.SetupSchemaAction -> Unit
            is Action.SetupPropertiesAction -> Unit
        }
    }

    private fun SetupEnvironmentAction.handle() {
        when (this) {
            is SetupEnvironmentAction.OnSelectEnvironmentPathClicked -> {
                val selectedEnvironmentPath = onSelectEnvironmentPathClicked()
                selectedEnvironmentPath?.let { path -> }
            }

            is SetupEnvironmentAction.OnEnvironmentNameChanged -> Unit
        }
    }

    override fun dispose() {
        ioScope.cancel()
    }
}