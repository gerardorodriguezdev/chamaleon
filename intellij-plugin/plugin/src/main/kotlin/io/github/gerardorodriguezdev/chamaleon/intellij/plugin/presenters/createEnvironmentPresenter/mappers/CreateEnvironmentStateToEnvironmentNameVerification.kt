package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.mappers

import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState

internal fun CreateEnvironmentState.toEnvironmentNameVerification(): EnvironmentNameVerification =
    when {
        environmentName.isEmpty() -> EnvironmentNameVerification.IS_EMPTY
        environmentName.isDuplicated(environments) -> EnvironmentNameVerification.IS_DUPLICATED
        else -> EnvironmentNameVerification.VALID
    }

private fun String.isDuplicated(environments: Set<Environment>): Boolean =
    environments.any { environment -> environment.name == this }

internal enum class EnvironmentNameVerification {
    VALID,
    IS_EMPTY,
    IS_DUPLICATED;

    fun isValid() = this == VALID
}