package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.EnvironmentsProcessorResult
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.SchemaParserResult
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.SetupEnvironmentProcessor.SetupEnvironmentProcessorResult
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

    override fun process(file: File): Flow<SetupEnvironmentProcessorResult> =
        flow {
            if (!file.isDirectory) {
                emit(SetupEnvironmentProcessorResult.Failure.FileIsNotDirectory)
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

    private suspend fun FlowCollector<SetupEnvironmentProcessorResult>.process(
        environmentsDirectory: File,
        environmentsDirectoryPath: String
    ) {
        emit(SetupEnvironmentProcessorResult.Loading(environmentsDirectoryPath))

        val result = environmentsProcessor.process(environmentsDirectory)
        val processorResult = result.toProcessorResult()
        emit(processorResult)
    }

    private fun EnvironmentsProcessorResult.toProcessorResult(): SetupEnvironmentProcessorResult =
        when (this) {
            is EnvironmentsProcessorResult.Success -> SetupEnvironmentProcessorResult.Success(
                environments = environments,
                schema = schema,
            )

            is EnvironmentsProcessorResult.Failure -> toProcessorResult()
        }

    private fun EnvironmentsProcessorResult.Failure.toProcessorResult(): SetupEnvironmentProcessorResult {
        return when (this) {
            is EnvironmentsProcessorResult.Failure.EnvironmentsDirectoryNotFound ->
                SetupEnvironmentProcessorResult.Failure.EnvironmentsDirectoryNotFound

            is EnvironmentsProcessorResult.Failure.SchemaParsingError -> toProcessorResult()
            else -> SetupEnvironmentProcessorResult.Failure.InvalidEnvironments
        }
    }

    private fun EnvironmentsProcessorResult.Failure.SchemaParsingError.toProcessorResult(): SetupEnvironmentProcessorResult =
        when (schemaParsingError) {
            is SchemaParserResult.Failure.FileNotFound -> SetupEnvironmentProcessorResult.Failure.SchemaFileNotFound
            else -> SetupEnvironmentProcessorResult.Failure.InvalidEnvironments
        }
}