package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.handlers

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.EnvironmentsProcessorResult
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.SchemaParserResult
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.handlers.SetupEnvironmentHandler.Action
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.handlers.SetupEnvironmentHandler.SideEffect
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.handlers.SetupEnvironmentHandler.SideEffect.UpdateEnvironmentsDirectoryState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.handlers.SetupEnvironmentHandler.SideEffect.UpdateEnvironmentsDirectoryState.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

internal interface SetupEnvironmentHandler {
    fun handle(action: Action): Flow<SideEffect>

    sealed interface Action {
        data class Process(val environmentsDirectory: File) : Action
    }

    sealed interface SideEffect {
        sealed interface UpdateEnvironmentsDirectoryState : SideEffect {
            data class Loading(val environmentsDirectoryPath: String) : UpdateEnvironmentsDirectoryState
            data class Success(
                val environmentsDirectoryPath: String,
                val environments: Set<Environment>,
                val schema: Schema,
            ) : UpdateEnvironmentsDirectoryState

            sealed interface Failure : UpdateEnvironmentsDirectoryState {
                data class FileIsNotDirectory(
                    val environmentsDirectoryPath: String,
                ) : Failure

                data class InvalidEnvironments(
                    val environmentsDirectoryPath: String,
                ) : Failure
            }
        }
    }
}

internal class DefaultSetupEnvironmentHandler(
    private val projectDirectory: File,
    private val environmentsProcessor: EnvironmentsProcessor,
) : SetupEnvironmentHandler {
    override fun handle(action: Action): Flow<SideEffect> =
        when (action) {
            is Action.Process -> handleProcess(action.environmentsDirectory)
        }

    private fun handleProcess(file: File): Flow<UpdateEnvironmentsDirectoryState> =
        flow {
            if (!file.isDirectory) {
                emit(Failure.FileIsNotDirectory(file.path))
                return@flow
            }

            val environmentsDirectory = file.toEnvironmentsDirectory()
            val environmentsDirectoryPath = environmentsDirectory.path.removePrefix(projectDirectory.path)

            emit(Loading(environmentsDirectoryPath))
            val updateEnvironmentsDirectoryState = handleProcess(
                environmentsDirectory = environmentsDirectory,
                environmentsDirectoryPath = environmentsDirectoryPath
            )
            emit(updateEnvironmentsDirectoryState)
        }

    private suspend fun handleProcess(
        environmentsDirectory: File,
        environmentsDirectoryPath: String
    ): UpdateEnvironmentsDirectoryState =
        environmentsProcessor
            .process(environmentsDirectory)
            .toUpdateEnvironmentsDirectoryState(environmentsDirectoryPath)

    private fun EnvironmentsProcessorResult.toUpdateEnvironmentsDirectoryState(
        environmentsDirectoryPath: String,
    ): UpdateEnvironmentsDirectoryState =
        when (this) {
            is EnvironmentsProcessorResult.Success ->
                Success(
                    environmentsDirectoryPath = environmentsDirectoryPath,
                    environments = environments,
                    schema = schema,
                )

            is EnvironmentsProcessorResult.Failure -> toUpdateEnvironmentsDirectoryState(environmentsDirectoryPath)
        }

    private fun EnvironmentsProcessorResult.Failure.toUpdateEnvironmentsDirectoryState(
        environmentsDirectoryPath: String,
    ): UpdateEnvironmentsDirectoryState =
        when (this) {
            is EnvironmentsProcessorResult.Failure.EnvironmentsDirectoryNotFound ->
                Success(
                    environmentsDirectoryPath = environmentsDirectoryPath,
                    environments = emptySet(),
                    schema = Schema.emptySchema(),
                )

            is EnvironmentsProcessorResult.Failure.SchemaParsingError -> toUpdateEnvironmentsDirectoryState(
                environmentsDirectoryPath
            )

            else -> Failure.InvalidEnvironments(environmentsDirectoryPath)
        }

    private fun EnvironmentsProcessorResult.Failure.SchemaParsingError.toUpdateEnvironmentsDirectoryState(
        environmentsDirectoryPath: String,
    ): UpdateEnvironmentsDirectoryState =
        when (schemaParsingError) {
            is SchemaParserResult.Failure.FileNotFound -> Success(
                environmentsDirectoryPath = environmentsDirectoryPath,
                environments = emptySet(),
                schema = Schema.emptySchema(),
            )

            else -> Failure.InvalidEnvironments(environmentsDirectoryPath)
        }

    private fun File.toEnvironmentsDirectory(): File =
        if (containsEnvironmentsDirectoryName()) this else appendEnvironmentsDirectoryName()

    private fun File.containsEnvironmentsDirectoryName(): Boolean =
        path.endsWith(EnvironmentsProcessor.ENVIRONMENTS_DIRECTORY_NAME)

    private fun File.appendEnvironmentsDirectoryName(): File =
        File(this, EnvironmentsProcessor.ENVIRONMENTS_DIRECTORY_NAME)
}