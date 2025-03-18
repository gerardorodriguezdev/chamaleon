package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.environmentSelectionPresenter

import io.github.gerardorodriguezdev.chamaleon.core.models.Project
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString

data class EnvironmentSelectionState(
    val isLoading: Boolean = false,
    val errorMessage: NonEmptyString? = null,
    val projects: NonEmptyKeySetStore<String, Project>? = null,
)