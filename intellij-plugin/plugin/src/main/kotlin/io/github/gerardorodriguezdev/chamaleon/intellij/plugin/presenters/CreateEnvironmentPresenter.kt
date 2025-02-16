package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters

import com.intellij.openapi.Disposable
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.EnvironmentsProcessorResult
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.SchemaParserResult
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.BaseDialog.DialogAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.extensions.isValid
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsProvider
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.Verification
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.Action
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.Action.SetupEnvironmentAction
import kotlinx.coroutines.*
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
    private val _state = MutableStateFlow<CreateEnvironmentState>(CreateEnvironmentState())
    val state: StateFlow<CreateEnvironmentState> = _state

    private val ioScope = CoroutineScope(ioDispatcher)
    private var processingJob: Job? = null

    init {
        process(file = projectDirectory)
    }

    fun onAction(action: Action) {
        when (action) {
            is SetupEnvironmentAction -> action.handle()
            is Action.SetupSchemaAction -> Unit
            is Action.SetupPropertiesAction -> Unit
        }
    }

    fun onDialogAction(action: DialogAction) {
        when (action) {
            is DialogAction.OnPreviousButtonClicked -> Unit
            is DialogAction.OnNextButtonClicked -> Unit
            is DialogAction.OnFinishButtonClicked -> Unit
        }
    }

    private fun process(file: File) {
        if (!file.isDirectory) {
            _state.value = _state.value.copy(
                environmentsDirectoryVerification = Verification.Invalid("Invalid directory selected"), //TODO: Abc
                environmentsDirectoryPath = null,
                isNextButtonEnabled = false,
                environments = null,
                schema = null,
            )
            return
        }

        val environmentsDirectory = file.toEnvironmentsDirectory()
        val environmentsDirectoryPath = environmentsDirectory.path.removePrefix(projectDirectory.path)
        process(environmentsDirectory = environmentsDirectory, environmentsDirectoryPath = environmentsDirectoryPath)
    }

    private fun process(environmentsDirectory: File, environmentsDirectoryPath: String) {
        processingJob?.cancel()

        _state.value = _state.value.copy(
            environmentsDirectoryVerification = Verification.InProgress,
            environmentsDirectoryPath = environmentsDirectoryPath,
        )

        processingJob = ioScope.launch {
            val result = environmentsProcessor.process(environmentsDirectory)
            val processingResult = result.toProcessingResult()

            withContext(uiDispatcher) {
                when (processingResult) {
                    is ProcessingResult.Success -> {
                        _state.value = _state.value.copy(
                            environmentsDirectoryVerification = Verification.Valid,
                            isNextButtonEnabled = _state.value.environmentNameVerification.isValid(),

                            environments = processingResult.environments,
                            schema = processingResult.schema,
                        )
                    }

                    is ProcessingResult.EnvironmentsDirectoryNotFound,
                    is ProcessingResult.SchemaFileNotFound -> {
                        _state.value = _state.value.copy(
                            environmentsDirectoryVerification = Verification.Valid,
                            isNextButtonEnabled = _state.value.environmentNameVerification.isValid(),

                            environments = null,
                            schema = null,
                        )
                    }

                    is ProcessingResult.InvalidEnvironments -> {
                        _state.value = _state.value.copy(
                            environmentsDirectoryVerification = Verification.Invalid(processingResult.reason),
                            isNextButtonEnabled = false,

                            environments = null,
                            schema = null,
                        )
                    }
                }
            }
        }
    }

    private fun SetupEnvironmentAction.handle() {
        when (this) {
            is SetupEnvironmentAction.OnSelectEnvironmentPathClicked -> {
                val selectedEnvironmentPath = onSelectEnvironmentPathClicked()
                selectedEnvironmentPath?.let { path ->
                    val file = File(path)
                    process(file = file)
                }
            }

            is SetupEnvironmentAction.OnEnvironmentNameChanged -> {
                val environmentNameVerification = newName.environmentNameVerification()
                _state.value = _state.value.copy(
                    environmentName = newName,
                    environmentNameVerification = environmentNameVerification,
                    isNextButtonEnabled =
                        _state.value.environmentsDirectoryVerification.isValid() && environmentNameVerification.isValid(),
                )
            }
        }
    }

    private fun String.environmentNameVerification(): Verification {
        val isEnvironmentNameDuplicated =
            _state.value.environments?.any { environment -> environment.name == this } == true

        return when {
            isEmpty() -> Verification.Invalid("Empty environmentName") //TODO: Abc
            isEnvironmentNameDuplicated -> Verification.Invalid("Duplicated environment") //TODO: Abc
            else -> Verification.Valid
        }
    }

    override fun dispose() {
        ioScope.cancel()
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

    private fun File.toEnvironmentsDirectory(): File =
        if (containsEnvironmentsDirectoryName()) this else appendEnvironmentsDirectoryName()

    private fun File.appendEnvironmentsDirectoryName(): File =
        File(this, EnvironmentsProcessor.ENVIRONMENTS_DIRECTORY_NAME)

    private fun File.containsEnvironmentsDirectoryName(): Boolean =
        path.endsWith(EnvironmentsProcessor.ENVIRONMENTS_DIRECTORY_NAME)

    data class CreateEnvironmentState(
        val environmentsDirectoryVerification: Verification? = null,
        val environmentName: String? = null,
        val environmentNameVerification: Verification? = null,

        val step: Step = Step.SETUP_ENVIRONMENT,

        val environmentsDirectoryPath: String? = null,
        val environments: Set<Environment>? = null,
        val schema: Schema? = null,

        val isPreviousButtonEnabled: Boolean = false,
        val isNextButtonEnabled: Boolean = false,
        val isFinishButtonEnabled: Boolean = false,
    ) {
        enum class Step {
            SETUP_ENVIRONMENT,
        }
    }

    sealed interface ProcessingResult {
        data class Success(
            val environments: Set<Environment>,
            val schema: Schema,
        ) : ProcessingResult

        data object EnvironmentsDirectoryNotFound : ProcessingResult
        data object SchemaFileNotFound : ProcessingResult
        data class InvalidEnvironments(val reason: String) : ProcessingResult
    }
}