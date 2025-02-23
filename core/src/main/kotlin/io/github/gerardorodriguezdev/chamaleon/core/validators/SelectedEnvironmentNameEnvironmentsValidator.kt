package io.github.gerardorodriguezdev.chamaleon.core.validators

import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsProcessorResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsProcessorResult.Failure.SelectedEnvironmentNotFound
import io.github.gerardorodriguezdev.chamaleon.core.utils.containsBy

internal fun String.isSelectedEnvironmentValidOrFailure(
    environmentsDirectoryPath: String,
    environments: Set<Environment>
): Failure? =
    if (!environments.containsBy { environment -> environment.name == this }) {
        SelectedEnvironmentNotFound(
            environmentsDirectoryPath = environmentsDirectoryPath,
            selectedEnvironmentName = this,
            environmentNames = environments.joinToString { environment -> environment.name }
        )
    } else null