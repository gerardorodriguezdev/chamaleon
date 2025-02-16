package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter

import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.extensions.isValid
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.Verification

data class CreateEnvironmentState(
    val environmentsDirectoryVerification: Verification? = null,
    val environmentName: String? = null,
    val environmentNameVerification: Verification? = null,

    val step: Step = Step.SETUP_ENVIRONMENT,

    val environmentsDirectoryPath: String? = null,
    val environments: Set<Environment>? = null,
    val schema: Schema? = null,
) {
    fun isPreviousButtonEnabled(): Boolean =
        when (step) {
            Step.SETUP_ENVIRONMENT -> false
        }

    fun isNextButtonEnabled(): Boolean =
        when (step) {
            Step.SETUP_ENVIRONMENT ->
                environmentsDirectoryVerification.isValid() && environmentNameVerification.isValid()
        }

    fun isFinishButtonEnabled(): Boolean =
        when (step) {
            Step.SETUP_ENVIRONMENT -> false
        }

    enum class Step {
        SETUP_ENVIRONMENT,
    }
}