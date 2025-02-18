package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.delegates

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.EnvironmentsProcessorResult
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.SchemaParserResult
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.delegates.SetupEnvironmentProcessor.SetupEnvironmentProcessorResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

internal interface SetupEnvironmentProcessor {
    fun process(file: File): Flow<SetupEnvironmentProcessorResult>

    sealed interface SetupEnvironmentProcessorResult {
        val environmentsDirectoryPath: String

        data class Success(
            override val environmentsDirectoryPath: String,
            val environments: Set<Environment>,
            val schema: Schema,
        ) : SetupEnvironmentProcessorResult

        data class Loading(override val environmentsDirectoryPath: String) : SetupEnvironmentProcessorResult
        sealed interface Failure : SetupEnvironmentProcessorResult {
            data class EnvironmentsDirectoryNotFound(override val environmentsDirectoryPath: String) : Failure
            data class SchemaFileNotFound(override val environmentsDirectoryPath: String) : Failure
            data class FileIsNotDirectory(override val environmentsDirectoryPath: String) : Failure
            data class InvalidEnvironments(override val environmentsDirectoryPath: String) : Failure
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
                emit(SetupEnvironmentProcessorResult.Failure.FileIsNotDirectory(file.path))
                return@flow
            }

            val environmentsDirectory = file.toEnvironmentsDirectory()
            val environmentsDirectoryPath = environmentsDirectory.path.removePrefix(projectDirectory.path)

            emit(SetupEnvironmentProcessorResult.Loading(environmentsDirectoryPath))
            val result = process(
                environmentsDirectory = environmentsDirectory,
                environmentsDirectoryPath = environmentsDirectoryPath
            )
            emit(result)
        }

    private fun File.toEnvironmentsDirectory(): File =
        if (containsEnvironmentsDirectoryName()) this else appendEnvironmentsDirectoryName()

    private fun File.appendEnvironmentsDirectoryName(): File =
        File(this, EnvironmentsProcessor.ENVIRONMENTS_DIRECTORY_NAME)

    private fun File.containsEnvironmentsDirectoryName(): Boolean =
        path.endsWith(EnvironmentsProcessor.ENVIRONMENTS_DIRECTORY_NAME)

    private suspend fun process(
        environmentsDirectory: File,
        environmentsDirectoryPath: String
    ): SetupEnvironmentProcessorResult {
        val result = environmentsProcessor.process(environmentsDirectory)
        val processorResult = result.toProcessorResult(environmentsDirectoryPath)
        return processorResult
    }

    private fun EnvironmentsProcessorResult.toProcessorResult(
        environmentsDirectoryPath: String,
    ): SetupEnvironmentProcessorResult =
        when (this) {
            is EnvironmentsProcessorResult.Success -> SetupEnvironmentProcessorResult.Success(
                environmentsDirectoryPath = environmentsDirectoryPath,
                environments = environments,
                schema = schema,
            )

            is EnvironmentsProcessorResult.Failure -> toProcessorResult(environmentsDirectoryPath)
        }

    private fun EnvironmentsProcessorResult.Failure.toProcessorResult(
        environmentsDirectoryPath: String,
    ): SetupEnvironmentProcessorResult {
        return when (this) {
            is EnvironmentsProcessorResult.Failure.EnvironmentsDirectoryNotFound ->
                SetupEnvironmentProcessorResult.Failure.EnvironmentsDirectoryNotFound(environmentsDirectoryPath)

            is EnvironmentsProcessorResult.Failure.SchemaParsingError -> toProcessorResult(environmentsDirectoryPath)
            else -> SetupEnvironmentProcessorResult.Failure.InvalidEnvironments(environmentsDirectoryPath)
        }
    }

    private fun EnvironmentsProcessorResult.Failure.SchemaParsingError.toProcessorResult(
        environmentsDirectoryPath: String,
    ): SetupEnvironmentProcessorResult =
        when (schemaParsingError) {
            is SchemaParserResult.Failure.FileNotFound -> SetupEnvironmentProcessorResult.Failure.SchemaFileNotFound(
                environmentsDirectoryPath
            )

            else -> SetupEnvironmentProcessorResult.Failure.InvalidEnvironments(environmentsDirectoryPath)
        }
}