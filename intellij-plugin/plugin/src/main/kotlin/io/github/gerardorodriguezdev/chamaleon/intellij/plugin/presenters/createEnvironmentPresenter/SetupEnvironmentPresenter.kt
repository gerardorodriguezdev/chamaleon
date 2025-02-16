package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter

import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.base.StateHolder
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentAction.SetupEnvironmentAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.SetupEnvironmentProcessor.SetupEnvironmentProcessorResult
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsProvider
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.Verification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.CoroutineContext

internal class SetupEnvironmentPresenter(
    private val projectDirectory: File,
    private val uiScope: CoroutineScope,
    private val ioScope: CoroutineContext,
    private val setupEnvironmentProcessor: SetupEnvironmentProcessor,
    private val stringsProvider: StringsProvider,
    private val stateHolder: StateHolder<CreateEnvironmentState>,
    private val onSelectEnvironmentPathClicked: () -> String?,
) {
    private var processJob: Job? = null

    fun onAction(action: SetupEnvironmentAction) {
        when (action) {
            is SetupEnvironmentAction.OnInit -> action.handle()
            is SetupEnvironmentAction.OnSelectEnvironmentPathClicked -> action.handle()
            is SetupEnvironmentAction.OnEnvironmentNameChanged -> action.handle()
        }
    }

    private fun SetupEnvironmentAction.OnInit.handle() {
        process(file = projectDirectory)
    }

    private fun SetupEnvironmentAction.OnSelectEnvironmentPathClicked.handle() {
        val selectedEnvironmentPath = onSelectEnvironmentPathClicked()
        selectedEnvironmentPath?.let { path ->
            process(file = File(path))
        }
    }

    private fun SetupEnvironmentAction.OnEnvironmentNameChanged.handle() {
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
                        is SetupEnvironmentProcessorResult.Success -> updateSuccessProcessing(processingResult)

                        is SetupEnvironmentProcessorResult.Loading ->
                            updateProcessingLoading(processingResult.environmentsDirectoryPath)

                        is SetupEnvironmentProcessorResult.Failure.EnvironmentsDirectoryNotFound,
                        is SetupEnvironmentProcessorResult.Failure.SchemaFileNotFound ->
                            updateValidEmptyEnvironmentsDirectory()

                        is SetupEnvironmentProcessorResult.Failure.FileIsNotDirectory ->
                            updateInvalidEnvironmentsDirectory(
                                reason = stringsProvider.string(StringsKeys.selectedFileNotDirectory),
                                environmentsDirectoryPath = null,
                            )

                        is SetupEnvironmentProcessorResult.Failure.InvalidEnvironments ->
                            updateInvalidEnvironmentsDirectory(
                                reason = stringsProvider.string(StringsKeys.invalidEnvironmentsFound),
                                environmentsDirectoryPath = stateHolder.state.environmentsDirectoryPath,
                            )
                    }
                }
        }
    }

    private fun updateProcessingLoading(environmentsDirectoryPath: String) {
        stateHolder.updateState { currentState ->
            currentState.copy(
                environmentsDirectoryVerification = Verification.InProgress,
                environmentsDirectoryPath = environmentsDirectoryPath,
            )
        }
    }

    private fun updateSuccessProcessing(setupEnvironmentProcessorResult: SetupEnvironmentProcessorResult.Success) {
        stateHolder.updateState { currentState ->
            currentState.copy(
                environmentsDirectoryVerification = Verification.Valid,
                environments = setupEnvironmentProcessorResult.environments,
                schema = setupEnvironmentProcessorResult.schema,
            )
        }
    }

    private fun updateValidEmptyEnvironmentsDirectory() {
        stateHolder.updateState { currentState ->
            currentState.copy(
                environmentsDirectoryVerification = Verification.Valid,
                environments = null,
                schema = null,
            )
        }
    }

    private fun updateInvalidEnvironmentsDirectory(
        reason: String,
        environmentsDirectoryPath: String?
    ) {
        stateHolder.updateState { currentState ->
            currentState.copy(
                environmentsDirectoryVerification = Verification.Invalid(reason),
                environmentsDirectoryPath = environmentsDirectoryPath,
                environments = null,
                schema = null,
            )
        }
    }

    private fun updateEnvironmentName(newEnvironmentName: String) {
        val environmentNameVerification = newEnvironmentName.environmentNameVerification()

        stateHolder.updateState { currentState ->
            currentState.copy(
                environmentName = newEnvironmentName,
                environmentNameVerification = environmentNameVerification,
            )
        }
    }

    private fun String.environmentNameVerification(): Verification {
        val isEnvironmentNameDuplicated =
            stateHolder.state.environments?.any { environment -> environment.name == this } == true

        return when {
            isEmpty() -> Verification.Invalid(stringsProvider.string(StringsKeys.environmentNameEmpty))
            isEnvironmentNameDuplicated ->
                Verification.Invalid(stringsProvider.string(StringsKeys.environmentNameIsDuplicated))

            else -> Verification.Valid
        }
    }
}