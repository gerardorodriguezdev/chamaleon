package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.intellij.openapi.Disposable
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.CONVENTION_ENVIRONMENTS_DIRECTORY_NAME
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.EnvironmentCardState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.EnvironmentSelectionState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.*
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

    fun scanProject(projectDirectoryPath: String) {
        val currentState = _state.value
        if (currentState.isLoading) return

        _state.value = currentState.copy(isLoading = true)

        ioScope
            .launch {
                val projectDirectory = File(projectDirectoryPath)
                val environmentsDirectoriesPaths = projectDirectory.environmentsDirectoriesPaths()
                val environmentCardStates = environmentCardStates(projectDirectoryPath, environmentsDirectoriesPaths)

                withContext(uiDispatcher) {
                    _state.value = currentState.copy(
                        environmentCardStates = environmentCardStates,
                        isLoading = false,
                    )
                }
            }
    }

    private fun File.environmentsDirectoriesPaths(): List<String> =
        this
            .walkTopDown()
            .filter { file -> file.isDirectory && file.name == CONVENTION_ENVIRONMENTS_DIRECTORY_NAME }
            .map { file -> file.path }
            .toList()

    private suspend fun CoroutineScope.environmentCardStates(
        projectDirectoryPath: String,
        environmentsDirectoriesPaths: List<String>,
    ): ImmutableList<EnvironmentCardState> =
        environmentsDirectoriesPaths
            .map { environmentsDirectoryPath ->
                async {
                    val environmentsDirectory = File(environmentsDirectoryPath)
                    environmentsDirectory.toEnvironmentCardState(projectDirectoryPath)
                }
            }
            .awaitAll()
            .filterNotNull()
            .toPersistentList()

    private fun File.toEnvironmentCardState(projectDirectoryPath: String): EnvironmentCardState? =
        try {
            val environmentsProcessorResult = environmentsProcessor.process(this)
            EnvironmentCardState(
                environmentsDirectoryPath = path.removePrefix(projectDirectoryPath),
                selectedEnvironment = environmentsProcessorResult.selectedEnvironmentName,
                environments = environmentsProcessorResult
                    .environments
                    .map { environment -> environment.name }
                    .toPersistentList(),
            )
        } catch (_: Exception) {
            null
        }

    fun onSelectedEnvironmentChanged(
        projectDirectoryPath: String,
        environmentsDirectoryPath: String,
        newSelectedEnvironment: String?
    ) {
        val currentState = _state.value

        ioScope
            .launch {
                val environmentsDirectory = File(projectDirectoryPath + environmentsDirectoryPath)

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
        this
            .map { environmentCardState ->
                if (environmentsDirectoryPath == environmentCardState.environmentsDirectoryPath) {
                    environmentCardState.copy(selectedEnvironment = newSelectedEnvironment)
                } else {
                    environmentCardState
                }
            }
            .toPersistentList()

    override fun dispose() {
        ioScope.cancel()
    }
}