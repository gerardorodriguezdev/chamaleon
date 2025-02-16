package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.EnvironmentsProcessorResult
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.SchemaParserResult
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.base.StateHolder
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentAction.SetupEnvironmentAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsProvider
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.Verification
import kotlinx.coroutines.*
import java.io.File

//TODO: Processing outside and only update state here
internal class SetupEnvironmentPresenter(
    private val projectDirectory: File,

    private val uiDispatcher: CoroutineDispatcher,
    private val ioScope: CoroutineScope,

    private val environmentsProcessor: EnvironmentsProcessor,
    private val stringsProvider: StringsProvider,

    private val onSelectEnvironmentPathClicked: () -> String?,
    stateHolder: StateHolder<CreateEnvironmentState>,
) : StateHolder<CreateEnvironmentState> by stateHolder {

    private var processingJob: Job? = null

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

    private fun process(file: File) {
        val environmentsDirectory = file.toEnvironmentsDirectory()
        val environmentsDirectoryPath = environmentsDirectory.path.removePrefix(projectDirectory.path)
        process(environmentsDirectory = environmentsDirectory, environmentsDirectoryPath = environmentsDirectoryPath)
    }

    private fun File.toEnvironmentsDirectory(): File =
        if (containsEnvironmentsDirectoryName()) this else appendEnvironmentsDirectoryName()

    private fun File.appendEnvironmentsDirectoryName(): File =
        File(this, EnvironmentsProcessor.Companion.ENVIRONMENTS_DIRECTORY_NAME)

    private fun File.containsEnvironmentsDirectoryName(): Boolean =
        path.endsWith(EnvironmentsProcessor.Companion.ENVIRONMENTS_DIRECTORY_NAME)

    private fun process(environmentsDirectory: File, environmentsDirectoryPath: String) {
        processingJob?.cancel()

        updateProcessingLoading(environmentsDirectoryPath)

        processingJob = ioScope.launch {
            val result = environmentsProcessor.process(environmentsDirectory)
            val processingResult = result.toProcessingResult()

            withContext(uiDispatcher) {
                when (processingResult) {
                    is ProcessingResult.Success -> updateSuccessProcessing(processingResult)

                    is ProcessingResult.EnvironmentsDirectoryNotFound,
                    is ProcessingResult.SchemaFileNotFound -> updateValidEmptyEnvironmentsDirectory()

                    is ProcessingResult.InvalidEnvironments -> updateInvalidEnvironmentsDirectory(
                        reason = processingResult.reason,
                        environmentsDirectoryPath = mutableState.value.environmentsDirectoryPath,
                    )
                }
            }
        }
    }

    private fun EnvironmentsProcessorResult.toProcessingResult(): ProcessingResult =
        when (this) {
            is EnvironmentsProcessorResult.Success -> ProcessingResult.Success(
                environments = environments,
                schema = schema,
            )

            is EnvironmentsProcessorResult.Failure -> toProcessingResult()
        }

    private fun EnvironmentsProcessorResult.Failure.toProcessingResult(): ProcessingResult {
        return when (this) {
            is EnvironmentsProcessorResult.Failure.EnvironmentsDirectoryNotFound ->
                ProcessingResult.EnvironmentsDirectoryNotFound

            is EnvironmentsProcessorResult.Failure.SchemaParsingError -> toProcessingResult()
            else -> genericInvalidEnvironments()
        }
    }

    private fun EnvironmentsProcessorResult.Failure.SchemaParsingError.toProcessingResult(): ProcessingResult =
        when (schemaParsingError) {
            is SchemaParserResult.Failure.FileNotFound -> ProcessingResult.SchemaFileNotFound
            else -> genericInvalidEnvironments()
        }

    private fun genericInvalidEnvironments(): ProcessingResult.InvalidEnvironments =
        ProcessingResult.InvalidEnvironments(reason = stringsProvider.string(StringsKeys.invalidEnvironmentsFound))

    sealed interface ProcessingResult {
        data class Success(
            val environments: Set<Environment>,
            val schema: Schema,
        ) : ProcessingResult

        data object EnvironmentsDirectoryNotFound : ProcessingResult
        data object SchemaFileNotFound : ProcessingResult
        data class InvalidEnvironments(val reason: String) : ProcessingResult
    }

    private fun SetupEnvironmentAction.OnSelectEnvironmentPathClicked.handle() {
        val selectedEnvironmentPath = onSelectEnvironmentPathClicked()
        selectedEnvironmentPath?.let { path ->
            val file = File(path)
            if (!file.isDirectory) {
                updateInvalidEnvironmentsDirectory(
                    reason = stringsProvider.string(StringsKeys.selectedFileNotDirectory),
                    environmentsDirectoryPath = null,
                )
            }

            process(file = file)
        }
    }

    private fun SetupEnvironmentAction.OnEnvironmentNameChanged.handle() {
        updateEnvironmentName(newName)
    }

    private fun updateProcessingLoading(environmentsDirectoryPath: String) {
        mutableState.value.copy(
            environmentsDirectoryVerification = Verification.InProgress,
            environmentsDirectoryPath = environmentsDirectoryPath,
        )
    }

    private fun updateSuccessProcessing(processingResult: ProcessingResult.Success) {
        mutableState.value.copy(
            environmentsDirectoryVerification = Verification.Valid,
            environments = processingResult.environments,
            schema = processingResult.schema,
        )
    }

    private fun updateValidEmptyEnvironmentsDirectory() {
        mutableState.value = mutableState.value.copy(
            environmentsDirectoryVerification = Verification.Valid,
            environments = null,
            schema = null,
        )
    }

    private fun updateInvalidEnvironmentsDirectory(
        reason: String,
        environmentsDirectoryPath: String?
    ) {
        mutableState.value = mutableState.value.copy(
            environmentsDirectoryVerification = Verification.Invalid(reason),
            environmentsDirectoryPath = environmentsDirectoryPath,
            environments = null,
            schema = null,
        )
    }

    private fun updateEnvironmentName(newEnvironmentName: String) {
        val environmentNameVerification = newEnvironmentName.environmentNameVerification()

        mutableState.value.copy(
            environmentName = newEnvironmentName,
            environmentNameVerification = environmentNameVerification,
        )
    }

    private fun String.environmentNameVerification(): Verification {
        val isEnvironmentNameDuplicated =
            mutableState.value.environments?.any { environment -> environment.name == this } == true

        return when {
            isEmpty() -> Verification.Invalid(stringsProvider.string(StringsKeys.environmentNameEmpty))
            isEnvironmentNameDuplicated ->
                Verification.Invalid(stringsProvider.string(StringsKeys.environmentNameIsDuplicated))

            else -> Verification.Valid
        }
    }
}