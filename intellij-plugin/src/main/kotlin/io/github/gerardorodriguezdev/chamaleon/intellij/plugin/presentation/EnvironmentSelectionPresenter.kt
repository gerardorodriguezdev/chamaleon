package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.intellij.openapi.Disposable
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.EnvironmentsProcessorResult
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.EnvironmentsProcessorResult.Success
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.EnvironmentCardState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.EnvironmentSelectionState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.CoroutineContext

class EnvironmentSelectionPresenter(
    private val environmentsProcessor: EnvironmentsProcessor,
    private val uiDispatcher: CoroutineContext,
    ioDispatcher: CoroutineContext,
    private val onEnvironmentsDirectoryChanged: (environmentsDirectory: File) -> Unit,
) : Disposable {
    private val _state = mutableStateOf(EnvironmentSelectionState())
    val state: State<EnvironmentSelectionState> = _state

    private val ioScope = CoroutineScope(ioDispatcher)

    fun scanProject(projectDirectory: File) {
        val currentState = _state.value
        if (currentState.isLoading) return

        _state.value = currentState.copy(isLoading = true)

        ioScope
            .launch {
                val environmentsProcessorResults = environmentsProcessor.processRecursively(projectDirectory)
                val environmentCardStates = environmentsProcessorResults.toEnvironmentCardStates(projectDirectory)

                withContext(uiDispatcher) {
                    _state.value = currentState.copy(
                        environmentCardStates = environmentCardStates,
                        isLoading = false,
                    )
                }
            }
    }

    private fun List<EnvironmentsProcessorResult>.toEnvironmentCardStates(
        projectDirectory: File,
    ): ImmutableList<EnvironmentCardState> =
        filterIsInstance<Success>()
            .map { success -> success.toEnvironmentCardState(projectDirectory) }
            .toPersistentList()

    private fun Success.toEnvironmentCardState(projectDirectory: File): EnvironmentCardState =
        EnvironmentCardState(
            environmentsDirectoryPath = environmentsDirectoryPath.removePrefix(projectDirectory.absolutePath),
            selectedEnvironment = selectedEnvironmentName,
            environments = environments
                .map { environment -> environment.name }
                .toPersistentList(),
        )

    fun onSelectedEnvironmentChanged(
        projectDirectory: File,
        environmentsDirectoryPath: String,
        newSelectedEnvironment: String?
    ) {
        val currentState = _state.value

        ioScope
            .launch {
                val environmentsDirectory = File(projectDirectory.absolutePath + environmentsDirectoryPath)

                val selectedEnvironmentUpdated = environmentsProcessor.updateSelectedEnvironment(
                    environmentsDirectory = environmentsDirectory,
                    newSelectedEnvironment = newSelectedEnvironment,
                )

                if (selectedEnvironmentUpdated) {
                    withContext(uiDispatcher) {
                        _state.value = currentState.copy(
                            environmentCardStates = currentState.environmentCardStates.updateEnvironmentCardState(
                                environmentsDirectoryPath = environmentsDirectoryPath,
                                newSelectedEnvironment = newSelectedEnvironment,
                            ),
                        )

                        onEnvironmentsDirectoryChanged(environmentsDirectory)
                    }
                }
            }
    }

    private fun ImmutableList<EnvironmentCardState>.updateEnvironmentCardState(
        environmentsDirectoryPath: String,
        newSelectedEnvironment: String?,
    ): ImmutableList<EnvironmentCardState> =
        map { environmentCardState ->
            if (environmentsDirectoryPath == environmentCardState.environmentsDirectoryPath) {
                environmentCardState.copy(selectedEnvironment = newSelectedEnvironment)
            } else {
                environmentCardState
            }
        }.toPersistentList()

    override fun dispose() {
        ioScope.cancel()
    }
}