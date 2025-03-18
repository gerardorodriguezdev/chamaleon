package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.environmentSelectionPresenter

import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString

sealed interface EnvironmentSelectionAction {
    data class ScanProject(val projectDirectory: ExistingDirectory) : EnvironmentSelectionAction
    data class SelectEnvironment(
        val index: Int,
        val newSelectedEnvironment: NonEmptyString?,
    ) : EnvironmentSelectionAction
}