package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.delegates

import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.base.StateHolder
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentAction.SetupEnvironmentAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentAction.SetupEnvironmentAction.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState.EnvironmentsDirectoryProcessResult.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.delegates.SetupEnvironmentProcessor.SetupEnvironmentProcessorResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.CoroutineContext

interface SetupEnvironmentPresenter {
    fun onAction(action: SetupEnvironmentAction)
}

internal class DefaultSetupEnvironmentPresenter(
    private val projectDirectory: File,
    private val uiScope: CoroutineScope,
    private val ioScope: CoroutineContext,
    setupEnvironmentProcessorProvider: (projectDirectory: File) -> SetupEnvironmentProcessor,
    private val stateHolder: StateHolder<CreateEnvironmentState>,
    private val onSelectEnvironmentPathClicked: () -> String?,
) : SetupEnvironmentPresenter {
    private val setupEnvironmentProcessor = setupEnvironmentProcessorProvider(projectDirectory)
    private var processJob: Job? = null

    override fun onAction(action: SetupEnvironmentAction) {
        when (action) {
            is OnInit -> action.handle()
            is OnSelectEnvironmentPathClicked -> action.handle()
            is OnEnvironmentNameChanged -> action.handle()
        }
    }

    private fun OnInit.handle() {
        process(file = projectDirectory)
    }

    private fun OnSelectEnvironmentPathClicked.handle() {
        val selectedEnvironmentPath = onSelectEnvironmentPathClicked()
        selectedEnvironmentPath?.let { path ->
            process(file = File(path))
        }
    }

    private fun OnEnvironmentNameChanged.handle() {
        updateEnvironmentName(newName)
    }

    private fun process(file: File) {
        processJob?.cancel()

        processJob = uiScope.launch {
            setupEnvironmentProcessor
                .process(file)
                .flowOn(ioScope)
                .collect { processorResult ->
                    when (processorResult) {
                        is SetupEnvironmentProcessorResult.Success -> processorResult.updateSuccess()

                        is SetupEnvironmentProcessorResult.Loading -> processorResult.updateLoading()

                        is SetupEnvironmentProcessorResult.Failure.EnvironmentsDirectoryNotFound,
                        is SetupEnvironmentProcessorResult.Failure.SchemaFileNotFound ->
                            processorResult.updateValidEmptyEnvironmentsDirectory()

                        is SetupEnvironmentProcessorResult.Failure.FileIsNotDirectory ->
                            processorResult.updateInvalidDirectory()

                        is SetupEnvironmentProcessorResult.Failure.InvalidEnvironments ->
                            processorResult.updateInvalidEnvironments()
                    }
                }
        }
    }

    private fun SetupEnvironmentProcessorResult.Loading.updateLoading() {
        stateHolder.updateState { currentState ->
            currentState.copy(
                environmentsDirectoryPath = environmentsDirectoryPath,
                environmentsDirectoryProcessResult = Loading
            )
        }
    }

    private fun SetupEnvironmentProcessorResult.Success.updateSuccess() {
        stateHolder.updateState { currentState ->
            currentState.copy(
                environmentsDirectoryPath = environmentsDirectoryPath,
                environmentsDirectoryProcessResult = Success,
                environments = environments,
                schema = schema,
            )
        }
    }

    private fun SetupEnvironmentProcessorResult.Failure.updateValidEmptyEnvironmentsDirectory() {
        stateHolder.updateState { currentState ->
            currentState.copy(
                environmentsDirectoryPath = environmentsDirectoryPath,
                environmentsDirectoryProcessResult = Success,
                environments = emptySet(),
                schema = Schema.emptySchema(),
            )
        }
    }

    private fun SetupEnvironmentProcessorResult.Failure.FileIsNotDirectory.updateInvalidDirectory() {
        stateHolder.updateState { currentState ->
            currentState.copy(
                environmentsDirectoryPath = environmentsDirectoryPath,
                environmentsDirectoryProcessResult = Failure.FileIsNotDirectory,
                environments = emptySet(),
                schema = Schema.emptySchema(),
            )
        }
    }

    private fun SetupEnvironmentProcessorResult.Failure.InvalidEnvironments.updateInvalidEnvironments() {
        stateHolder.updateState { currentState ->
            currentState.copy(
                environmentsDirectoryPath = environmentsDirectoryPath,
                environmentsDirectoryProcessResult = Failure.InvalidEnvironments,
                environments = emptySet(),
                schema = Schema.emptySchema(),
            )
        }
    }

    private fun updateEnvironmentName(newEnvironmentName: String) {
        stateHolder.updateState { currentState ->
            currentState.copy(
                environmentName = newEnvironmentName,
            )
        }
    }
}