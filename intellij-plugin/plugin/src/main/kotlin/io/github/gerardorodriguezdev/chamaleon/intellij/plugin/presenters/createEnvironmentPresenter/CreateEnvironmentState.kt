package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter

import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.Verification

data class CreateEnvironmentState(
    val environmentsDirectoryVerification: Verification? = null,
    val environmentName: String? = null,
    val environmentNameVerification: Verification? = null,

    val step: Step = Step.SETUP_ENVIRONMENT,

    val environmentsDirectoryPath: String? = null,
    val environments: Set<Environment>? = null,
    val schema: Schema? = null,

    val isPreviousButtonEnabled: Boolean = false,
    val isNextButtonEnabled: Boolean = false,
    val isFinishButtonEnabled: Boolean = false,
) {
    enum class Step {
        SETUP_ENVIRONMENT,
    }
}