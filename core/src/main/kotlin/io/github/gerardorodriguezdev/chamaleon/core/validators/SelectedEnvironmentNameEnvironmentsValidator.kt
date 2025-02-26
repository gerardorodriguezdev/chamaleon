package io.github.gerardorodriguezdev.chamaleon.core.validators

import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsProcessorResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsProcessorResult.Failure.SelectedEnvironmentNotFound

internal fun String.isSelectedEnvironmentValidOrFailure(
    environmentsDirectoryPath: String,
    environmentsMap: Map<String, Environment>
): Failure? =
    if (!environmentsMap.contains(this)) {
        SelectedEnvironmentNotFound(
            environmentsDirectoryPath = environmentsDirectoryPath,
            selectedEnvironmentName = this,
            environmentNames = environmentsMap.values.joinToString { environment -> environment.name }
        )
    } else {
        null
    }