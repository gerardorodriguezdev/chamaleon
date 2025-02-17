package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.delegates

import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.base.StateHolder
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentAction.SetupEnvironmentAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentAction.SetupEnvironmentAction.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState.Field
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsProvider
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.Verification
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
    private val stringsProvider: StringsProvider,
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
                .collect { processingResult ->
                    when (processingResult) {
                        is SetupEnvironmentProcessor.SetupEnvironmentProcessorResult.Success ->
                            processingResult.updateSuccessProcessing()

                        is SetupEnvironmentProcessor.SetupEnvironmentProcessorResult.Loading ->
                            processingResult.updateProcessingLoading()

                        is SetupEnvironmentProcessor.SetupEnvironmentProcessorResult.Failure.EnvironmentsDirectoryNotFound,
                        is SetupEnvironmentProcessor.SetupEnvironmentProcessorResult.Failure.SchemaFileNotFound ->
                            updateValidEmptyEnvironmentsDirectory()

                        is SetupEnvironmentProcessor.SetupEnvironmentProcessorResult.Failure.FileIsNotDirectory ->
                            updateInvalidEnvironmentsDirectory(
                                reason = stringsProvider.string(StringsKeys.selectedFileNotDirectory),
                                environmentsDirectoryPath = "",
                            )

                        is SetupEnvironmentProcessor.SetupEnvironmentProcessorResult.Failure.InvalidEnvironments ->
                            updateInvalidEnvironmentsDirectory(
                                reason = stringsProvider.string(StringsKeys.invalidEnvironmentsFound),
                                environmentsDirectoryPath = stateHolder.state.environmentsDirectoryPathField.value,
                            )
                    }
                }
        }
    }

    private fun SetupEnvironmentProcessor.SetupEnvironmentProcessorResult.Loading.updateProcessingLoading() {
        stateHolder.updateState { currentState ->
            currentState.copy(
                environmentsDirectoryPathField = Field(
                    value = environmentsDirectoryPath,
                    verification = Verification.InProgress,
                )
            )
        }
    }

    private fun SetupEnvironmentProcessor.SetupEnvironmentProcessorResult.Success.updateSuccessProcessing() {
        stateHolder.updateState { currentState ->
            currentState.copy(
                environmentsDirectoryPathField =
                    currentState.environmentsDirectoryPathField.copy(verification = Verification.Valid),
                environments = environments,
                schema = schema,
            )
        }
    }

    private fun updateValidEmptyEnvironmentsDirectory() {
        stateHolder.updateState { currentState ->
            currentState.copy(
                environmentsDirectoryPathField =
                    currentState.environmentsDirectoryPathField.copy(verification = Verification.Valid),
                environments = emptySet(),
                schema = Schema.emptySchema(),
            )
        }
    }

    private fun updateInvalidEnvironmentsDirectory(
        reason: String,
        environmentsDirectoryPath: String
    ) {
        stateHolder.updateState { currentState ->
            currentState.copy(
                environmentsDirectoryPathField = Field(
                    value = environmentsDirectoryPath,
                    verification = Verification.Invalid(reason),
                ),
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