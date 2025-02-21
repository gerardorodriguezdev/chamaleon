package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.intellij.openapi.Disposable
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.AddOrUpdateSelectedEnvironmentResult
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.EnvironmentsProcessorResult
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.EnvironmentsProcessorResult.Success
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.Versions
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsProvider
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

internal class EnvironmentSelectionPresenter(
    private val stringsProvider: StringsProvider,
    private val environmentsProcessor: EnvironmentsProcessor,
    private val uiDispatcher: CoroutineContext,
    ioDispatcher: CoroutineContext,
    private val onEnvironmentsDirectoryChanged: (environmentsDirectory: File) -> Unit,
) : Disposable {
    private val mutableState =
        mutableStateOf(EnvironmentSelectionState(gradlePluginVersionUsed = Versions.GRADLE_PLUGIN))
    val state: State<EnvironmentSelectionState> = mutableState

    private val ioScope = CoroutineScope(ioDispatcher)

    fun scanProject(projectDirectory: File) {
        if (mutableState.value.isLoading) return

        mutableState.value = mutableState.value.copy(isLoading = true)

        ioScope
            .launch {
                val environmentsProcessorResults = environmentsProcessor.processRecursively(projectDirectory)
                val notificationErrorMessage = environmentsProcessorResults.toNotificationErrorMessage()
                val environmentCardStates = environmentsProcessorResults.toEnvironmentCardStates(projectDirectory)

                withContext(uiDispatcher) {
                    mutableState.value = mutableState.value.copy(
                        notificationErrorMessage = notificationErrorMessage,
                        environmentCardStates = environmentCardStates,
                        isLoading = false,
                    )
                }
            }
    }

    private fun List<EnvironmentsProcessorResult>.toNotificationErrorMessage(): String? {
        val environmentsDirectoryPathsWithErrors =
            filterIsInstance<EnvironmentsProcessorResult.Failure>().map { failure -> failure.environmentsDirectoryPath }
        if (environmentsDirectoryPathsWithErrors.isEmpty()) return null
        return buildString {
            appendLine(stringsProvider.string(StringsKeys.errorAtEnvironmentsDirectories))

            environmentsDirectoryPathsWithErrors.forEach { environmentsDirectoryPath ->
                appendLine("- $environmentsDirectoryPath")
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
            environmentsDirectoryPath = environmentsDirectoryPath.removePrefix(projectDirectory.path),
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
        ioScope
            .launch {
                val environmentsDirectory = File(projectDirectory.path + environmentsDirectoryPath)

                val addOrUpdateSelectedEnvironmentResult = environmentsProcessor.addOrUpdateSelectedEnvironment(
                    environmentsDirectory = environmentsDirectory,
                    newSelectedEnvironment = newSelectedEnvironment,
                )

                if (addOrUpdateSelectedEnvironmentResult is AddOrUpdateSelectedEnvironmentResult.Success) {
                    withContext(uiDispatcher) {
                        mutableState.value = mutableState.value.copy(
                            environmentCardStates = mutableState.value.environmentCardStates.updateEnvironmentCardState(
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