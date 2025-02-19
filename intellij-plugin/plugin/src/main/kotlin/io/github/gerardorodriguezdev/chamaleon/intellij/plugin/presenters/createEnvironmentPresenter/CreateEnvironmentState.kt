package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter

import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema

internal data class CreateEnvironmentState(
    val environmentsDirectoryPath: String = "",
    val environmentsDirectoryProcessResult: EnvironmentsDirectoryProcessResult =
        EnvironmentsDirectoryProcessResult.Loading,
    val environmentName: String = "",
    val environments: Set<Environment> = emptySet(),
    val schema: Schema = Schema(
        supportedPlatforms = emptySet(),
        propertyDefinitions = emptySet(),
    ),
    val step: Step = Step.SETUP_ENVIRONMENT,
) {
    sealed interface EnvironmentsDirectoryProcessResult {
        data object Success : EnvironmentsDirectoryProcessResult
        data object Loading : EnvironmentsDirectoryProcessResult
        sealed interface Failure : EnvironmentsDirectoryProcessResult {
            data object EnvironmentsDirectoryNotFound : Failure
            data object SchemaFileNotFound : Failure
            data object FileIsNotDirectory : Failure
            data object InvalidEnvironments : Failure
        }
    }

    enum class Step {
        SETUP_ENVIRONMENT,
        SETUP_SCHEMA,
        SETUP_PROPERTIES,
    }
}