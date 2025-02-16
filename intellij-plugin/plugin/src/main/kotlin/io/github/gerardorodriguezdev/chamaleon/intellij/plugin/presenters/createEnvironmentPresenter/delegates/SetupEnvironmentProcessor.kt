package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.delegates

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.EnvironmentsProcessorResult
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.SchemaParserResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import java.io.File

internal interface SetupEnvironmentProcessor {
    fun process(file: File): Flow<SetupEnvironmentProcessorResult>

    sealed interface SetupEnvironmentProcessorResult {
        data class Success(
            val environments: Set<Environment>,
            val schema: Schema,
        ) : SetupEnvironmentProcessorResult

        data class Loading(val environmentsDirectoryPath: String) : SetupEnvironmentProcessorResult
        sealed interface Failure : SetupEnvironmentProcessorResult {
            data object EnvironmentsDirectoryNotFound : Failure
            data object SchemaFileNotFound : Failure
            data object FileIsNotDirectory : Failure
            data object InvalidEnvironments : Failure
        }
    }
}

internal class DefaultSetupEnvironmentProcessor(
    private val projectDirectory: File,
    private val environmentsProcessor: EnvironmentsProcessor,
) : SetupEnvironmentProcessor {

    override fun process(file: File): Flow<SetupEnvironmentProcessor.SetupEnvironmentProcessorResult> =
        flow {
            if (!file.isDirectory) {
                emit(SetupEnvironmentProcessor.SetupEnvironmentProcessorResult.Failure.FileIsNotDirectory)
                return@flow
            }

            val environmentsDirectory = file.toEnvironmentsDirectory()
            val environmentsDirectoryPath = environmentsDirectory.path.removePrefix(projectDirectory.path)
            process(
                environmentsDirectory = environmentsDirectory,
                environmentsDirectoryPath = environmentsDirectoryPath
            )
        }

    private fun File.toEnvironmentsDirectory(): File =
        if (containsEnvironmentsDirectoryName()) this else appendEnvironmentsDirectoryName()

    private fun File.appendEnvironmentsDirectoryName(): File =
        File(this, EnvironmentsProcessor.Companion.ENVIRONMENTS_DIRECTORY_NAME)

    private fun File.containsEnvironmentsDirectoryName(): Boolean =
        path.endsWith(EnvironmentsProcessor.Companion.ENVIRONMENTS_DIRECTORY_NAME)

    private suspend fun FlowCollector<SetupEnvironmentProcessor.SetupEnvironmentProcessorResult>.process(
        environmentsDirectory: File,
        environmentsDirectoryPath: String
    ) {
        emit(SetupEnvironmentProcessor.SetupEnvironmentProcessorResult.Loading(environmentsDirectoryPath))

        val result = environmentsProcessor.process(environmentsDirectory)
        val processorResult = result.toProcessorResult()
        emit(processorResult)
    }

    private fun EnvironmentsProcessorResult.toProcessorResult(): SetupEnvironmentProcessor.SetupEnvironmentProcessorResult =
        when (this) {
            is EnvironmentsProcessorResult.Success -> SetupEnvironmentProcessor.SetupEnvironmentProcessorResult.Success(
                environments = environments,
                schema = schema,
            )

            is EnvironmentsProcessorResult.Failure -> toProcessorResult()
        }

    private fun EnvironmentsProcessorResult.Failure.toProcessorResult(): SetupEnvironmentProcessor.SetupEnvironmentProcessorResult {
        return when (this) {
            is EnvironmentsProcessorResult.Failure.EnvironmentsDirectoryNotFound ->
                SetupEnvironmentProcessor.SetupEnvironmentProcessorResult.Failure.EnvironmentsDirectoryNotFound

            is EnvironmentsProcessorResult.Failure.SchemaParsingError -> toProcessorResult()
            else -> SetupEnvironmentProcessor.SetupEnvironmentProcessorResult.Failure.InvalidEnvironments
        }
    }

    private fun EnvironmentsProcessorResult.Failure.SchemaParsingError.toProcessorResult(): SetupEnvironmentProcessor.SetupEnvironmentProcessorResult =
        when (schemaParsingError) {
            is SchemaParserResult.Failure.FileNotFound -> SetupEnvironmentProcessor.SetupEnvironmentProcessorResult.Failure.SchemaFileNotFound
            else -> SetupEnvironmentProcessor.SetupEnvironmentProcessorResult.Failure.InvalidEnvironments
        }
}