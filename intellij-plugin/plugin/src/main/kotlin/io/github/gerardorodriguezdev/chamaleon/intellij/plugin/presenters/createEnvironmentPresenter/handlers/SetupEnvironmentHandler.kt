package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.handlers

import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectDeserializationResult
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectDeserializer
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
        data class Process(val file: File) : Action
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
    private val projectDeserializer: ProjectDeserializer,
) : SetupEnvironmentHandler {
    override fun handle(action: Action): Flow<SideEffect> =
        when (action) {
            is Action.Process -> action.handle()
        }

    private fun Action.Process.handle(): Flow<UpdateEnvironmentsDirectoryState> =
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
        projectDeserializer
            .process(environmentsDirectory)
            .toUpdateEnvironmentsDirectoryState(environmentsDirectoryPath)

    private fun ProjectDeserializationResult.toUpdateEnvironmentsDirectoryState(
        environmentsDirectoryPath: String,
    ): UpdateEnvironmentsDirectoryState =
        when (this) {
            is ProjectDeserializationResult.Success ->
                Success(
                    environmentsDirectoryPath = environmentsDirectoryPath,
                    environments = environments,
                    schema = schema,
                )

            is ProjectDeserializationResult.Failure -> toUpdateEnvironmentsDirectoryState(environmentsDirectoryPath)
        }

    private fun ProjectDeserializationResult.Failure.toUpdateEnvironmentsDirectoryState(
        environmentsDirectoryPath: String,
    ): UpdateEnvironmentsDirectoryState =
        when (this) {
            is ProjectDeserializationResult.Failure.EnvironmentsDirectoryNotFound ->
                Success(
                    environmentsDirectoryPath = environmentsDirectoryPath,
                    environments = emptySet(),
                    schema = Schema(),
                )

            is ProjectDeserializationResult.Failure.SchemaParsing -> toUpdateEnvironmentsDirectoryState(
                environmentsDirectoryPath
            )

            else -> Failure.InvalidEnvironments(environmentsDirectoryPath)
        }

    private fun ProjectDeserializationResult.Failure.SchemaParsing.toUpdateEnvironmentsDirectoryState(
        environmentsDirectoryPath: String,
    ): UpdateEnvironmentsDirectoryState =
        when (error) {
            is SchemaParserResult.Failure.FileNotFound -> Success(
                environmentsDirectoryPath = environmentsDirectoryPath,
                environments = emptySet(),
                schema = Schema(),
            )

            else -> Failure.InvalidEnvironments(environmentsDirectoryPath)
        }

    private fun File.toEnvironmentsDirectory(): File =
        if (containsEnvironmentsDirectoryName()) this else appendEnvironmentsDirectoryName()

    private fun File.containsEnvironmentsDirectoryName(): Boolean =
        path.endsWith(ProjectDeserializer.ENVIRONMENTS_DIRECTORY_NAME)

    private fun File.appendEnvironmentsDirectoryName(): File =
        File(this, ProjectDeserializer.ENVIRONMENTS_DIRECTORY_NAME)
}