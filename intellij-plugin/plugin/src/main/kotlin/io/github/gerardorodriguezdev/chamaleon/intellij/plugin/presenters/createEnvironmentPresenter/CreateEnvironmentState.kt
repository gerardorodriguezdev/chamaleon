package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter

import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.extensions.isValid
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.Verification

internal data class CreateEnvironmentState(
    val environmentsDirectoryPath: String? = null,
    val environmentsDirectoryVerification: Verification? = null,

    val environmentName: String? = null,
    val environmentNameVerification: Verification? = null,

    val environments: Set<Environment>? = null,

    val schema: Schema? = null,
    val schemaVerification: Verification? = null,

    val step: Step = Step.SETUP_ENVIRONMENT,
) {
    fun isPreviousButtonEnabled(): Boolean =
        when (step) {
            Step.SETUP_ENVIRONMENT -> false
            Step.SETUP_SCHEMA -> true
        }

    fun isNextButtonEnabled(): Boolean =
        when (step) {
            Step.SETUP_ENVIRONMENT ->
                environmentsDirectoryVerification.isValid() && environmentNameVerification.isValid()

            Step.SETUP_SCHEMA -> schemaVerification.isValid()
        }

    fun isFinishButtonEnabled(): Boolean =
        when (step) {
            Step.SETUP_ENVIRONMENT -> false
            Step.SETUP_SCHEMA -> false
        }

    enum class Step {
        SETUP_ENVIRONMENT,
        SETUP_SCHEMA,
    }
}