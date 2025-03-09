package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter

import io.github.gerardorodriguezdev.chamaleon.core.models.*
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptySet
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString

internal sealed interface CreateProjectState {
    fun asSetupEnvironment(): SetupEnvironment = this as SetupEnvironment
    fun asSetupSchema(): SetupSchema = this as SetupSchema
    fun asSetupPlatforms(): SetupPlatforms = this as SetupPlatforms

    data class SetupEnvironment(
        val projectDeserializationState: ProjectDeserializationState? = null,
        val environmentName: NonEmptyString? = null,
    ) : CreateProjectState {
        sealed interface ProjectDeserializationState {
            val environmentsDirectory: ExistingDirectory

            data class Loading(
                override val environmentsDirectory: ExistingDirectory,
            ) : ProjectDeserializationState

            data class Invalid(
                override val environmentsDirectory: ExistingDirectory,
                val errorMessage: String
            ) : ProjectDeserializationState

            data class Valid(
                override val environmentsDirectory: ExistingDirectory,
                val project: Project?
            ) : ProjectDeserializationState
        }
    }

    data class SetupSchema(
        val environmentsDirectory: ExistingDirectory,
        val environmentName: NonEmptyString,
        val currentProject: Project?,
        val globalSupportedPlatformTypes: NonEmptySet<PlatformType>? = null,
        val propertyDefinitions: List<PropertyDefinition> = emptyList(),
    ) : CreateProjectState {
        data class PropertyDefinition(
            val name: NonEmptyString? = null,
            val propertyType: PropertyType = PropertyType.STRING,
            val nullable: Boolean = false,
            val supportedPlatformTypes: NonEmptySet<PlatformType>? = null,
        )
    }

    data class SetupPlatforms(
        val environmentsDirectory: ExistingDirectory,
        val environmentName: NonEmptyString,
        val currentProject: Project? = null,
        val schema: Schema,
        val platforms: NonEmptyKeySetStore<PlatformType, Platform>,
    ) : CreateProjectState
}