package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.environmentSelectionPresenter

import io.github.gerardorodriguezdev.chamaleon.core.models.Project
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectDeserializationResult
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectSerializationResult
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore.Companion.toNonEmptyKeySetStore
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptySet.Companion.toNonEmptySet
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString.Companion.toNonEmptyString
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectDeserializer
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectSerializer
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.asDelegate
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EnvironmentSelectionPresenter(
    private val stringsProvider: StringsProvider,
    private val projectSerializer: ProjectSerializer,
    private val projectDeserializer: ProjectDeserializer,
    private val uiScope: CoroutineScope,
    private val ioScope: CoroutineScope,
    private val onEnvironmentsDirectoryChanged: (newEnvironmentsDirectory: ExistingDirectory) -> Unit,
) {
    private val mutableStateFlow = MutableStateFlow<EnvironmentSelectionState>(EnvironmentSelectionState())
    val stateFlow: StateFlow<EnvironmentSelectionState> = mutableStateFlow

    private var mutableState by mutableStateFlow.asDelegate()

    private var scanProjectJob: Job? = null
    private var updateSelectedEnvironmentJob: Job? = null

    fun dispatch(action: EnvironmentSelectionAction) {
        when (action) {
            is EnvironmentSelectionAction.ScanProject -> action.handle()
            is EnvironmentSelectionAction.SelectEnvironment -> action.handle()
        }
    }

    private fun EnvironmentSelectionAction.ScanProject.handle() {
        scanProjectJob?.cancel()

        mutableState = mutableState.copy(isLoading = true)

        scanProjectJob = ioScope
            .launch {
                val projectDeserializationResults = projectDeserializer.deserializeRecursively(projectDirectory)
                val errorMessage = projectDeserializationResults.toErrorMessage()
                val projects = projectDeserializationResults.toProjects()

                withContext(uiScope.coroutineContext) {
                    mutableState = mutableState.copy(
                        errorMessage = errorMessage,
                        projects = projects,
                        isLoading = false,
                    )
                }
            }
    }

    private fun List<ProjectDeserializationResult>.toErrorMessage(): NonEmptyString? {
        val environmentsDirectoryPathsWithErrors =
            this
                .filterIsInstance<ProjectDeserializationResult.Failure>()
                .map { failure -> failure.environmentsDirectoryPath.toNonEmptyString() }
                .toNonEmptySet()
                ?: return null

        return buildString {
            appendLine(stringsProvider.string(StringsKeys.errorAtEnvironmentsDirectories))

            environmentsDirectoryPathsWithErrors
                .forEach { environmentsDirectoryPath ->
                    appendLine("- $environmentsDirectoryPath")
                }
        }.toNonEmptyString()
    }

    private fun List<ProjectDeserializationResult>.toProjects(): NonEmptyKeySetStore<String, Project>? =
        this
            .filterIsInstance<ProjectDeserializationResult.Success>()
            .map { success -> success.project }
            .toNonEmptyKeySetStore()

    private fun EnvironmentSelectionAction.SelectEnvironment.handle() {
        updateSelectedEnvironmentJob?.cancel()

        updateSelectedEnvironmentJob =
            ioScope
                .launch {
                    val currentProjects = mutableState.projects
                    val project = currentProjects?.values?.elementAtOrNull(index) ?: return@launch
                    val newProject = project.updateProperties(newSelectedEnvironment) ?: return@launch

                    val projectSerializationResult = projectSerializer.serialize(newProject)

                    when (projectSerializationResult) {
                        is ProjectSerializationResult.Success -> {
                            val newProjects = currentProjects.updateElementByKey(newProject)

                            withContext(uiScope.coroutineContext) {
                                mutableState = mutableState.copy(projects = newProjects)
                                onEnvironmentsDirectoryChanged(newProject.environmentsDirectory)
                            }
                        }

                        is ProjectSerializationResult.Failure -> Unit
                    }
                }
    }
}