package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.toolWindows.environmentSelectionToolWindow.mappers

import io.github.gerardorodriguezdev.chamaleon.core.Versions
import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.models.Project
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.environmentSelectionPresenter.EnvironmentSelectionState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.EnvironmentCardState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.EnvironmentSelectionWindowState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

internal fun EnvironmentSelectionState.toEnvironmentsSelectionWindowState(): EnvironmentSelectionWindowState =
    EnvironmentSelectionWindowState(
        gradlePluginVersionUsed = Versions.CORE,
        isLoading = isLoading,
        notificationErrorMessage = errorMessage?.value,
        environmentCardStates = projects.toEnvironmentCardStates(),
    )

private fun NonEmptyKeySetStore<String, Project>?.toEnvironmentCardStates(): ImmutableList<EnvironmentCardState> {
    if (this == null) return persistentListOf()

    return values.map { project ->
        EnvironmentCardState(
            environmentsDirectoryPath = project.environmentsDirectory.path.value,
            selectedEnvironment = project.selectedEnvironment()?.name?.value,
            environments = project.environments.toEnvironments(),
        )
    }.toPersistentList()
}

private fun NonEmptyKeySetStore<String, Environment>?.toEnvironments(): ImmutableList<String> {
    if (this == null) return persistentListOf()

    return values.map { environment -> environment.name.value }.toPersistentList()
}