package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.mappers

import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState

internal fun CreateEnvironmentState.toEnvironmentNameVerification(): EnvironmentNameVerification =
    when {
        environmentName.isEmpty() -> EnvironmentNameVerification.IS_EMPTY
        environmentsNames.contains(environmentName) -> EnvironmentNameVerification.IS_DUPLICATED
        else -> EnvironmentNameVerification.VALID
    }

internal enum class EnvironmentNameVerification {
    VALID,
    IS_EMPTY,
    IS_DUPLICATED;

    fun isValid() = this == VALID
}