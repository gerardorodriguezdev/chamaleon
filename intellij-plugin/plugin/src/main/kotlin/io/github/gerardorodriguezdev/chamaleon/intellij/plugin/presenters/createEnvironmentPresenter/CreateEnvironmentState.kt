package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter

import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.Verification

internal data class CreateEnvironmentState(
    val environmentsDirectoryPathField: Field<String> =
        Field(
            value = "",
            verification = null,
        ),

    val environmentName: String = "",

    val environments: Set<Environment> = emptySet(),

    val schema: Schema = Schema(
        supportedPlatforms = emptySet(),
        propertyDefinitions = emptySet(),
    ),

    val step: Step = Step.SETUP_ENVIRONMENT,
) {
    val environmentNameVerification: EnvironmentNameVerification
        get() = environmentName.environmentNameVerification()

    fun isPreviousButtonEnabled(): Boolean =
        when (step) {
            Step.SETUP_ENVIRONMENT -> false
            Step.SETUP_SCHEMA -> true
        }

    fun isNextButtonEnabled(): Boolean =
        when (step) {
            Step.SETUP_ENVIRONMENT -> environmentsDirectoryPathField.isValid() && environmentNameVerification.isValid()
            Step.SETUP_SCHEMA -> schema.areEnvironmentsValid(environments).areEnvironmentsValid()
        }

    fun isFinishButtonEnabled(): Boolean =
        when (step) {
            Step.SETUP_ENVIRONMENT -> false
            Step.SETUP_SCHEMA -> false
        }

    private fun List<Schema.EnvironmentsValidationResult>.areEnvironmentsValid(): Boolean =
        !any { environmentValidationResult ->
            environmentValidationResult is Schema.EnvironmentsValidationResult.Failure
        }

    private fun String.environmentNameVerification(): EnvironmentNameVerification =
        when {
            isEmpty() -> EnvironmentNameVerification.IS_EMPTY
            isDuplicated() -> EnvironmentNameVerification.IS_DUPLICATED
            else -> EnvironmentNameVerification.VALID
        }

    private fun String.isDuplicated(): Boolean = environments.any { environment -> environment.name == this }

    data class Field<T>(
        val value: T,
        val verification: Verification? = null,
    ) {
        fun isValid(): Boolean = verification.isValid()
    }

    enum class Step {
        SETUP_ENVIRONMENT,
        SETUP_SCHEMA,
    }

    enum class EnvironmentNameVerification {
        VALID,
        IS_EMPTY,
        IS_DUPLICATED;

        fun isValid(): Boolean = this == VALID
    }
}