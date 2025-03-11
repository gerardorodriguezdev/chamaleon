package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter

import io.github.gerardorodriguezdev.chamaleon.core.models.*
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptySet
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString

sealed interface CreateProjectState {
    fun asSetupEnvironment(): SetupEnvironment? = this as? SetupEnvironment
    fun asSetupSchemaNewSchema(): SetupSchema.NewSchema? = this as? SetupSchema.NewSchema
    fun asSetupPlatforms(): SetupPlatforms? = this as? SetupPlatforms

    data class SetupEnvironment(
        val projectDeserializationState: ProjectDeserializationState? = null,
        val environmentName: NonEmptyString? = null,
    ) : CreateProjectState {
        sealed interface ProjectDeserializationState {
            data class Loading(
                val environmentsDirectory: ExistingDirectory,
            ) : ProjectDeserializationState

            data class Invalid(
                val environmentsDirectory: ExistingDirectory,
                val errorMessage: String
            ) : ProjectDeserializationState

            sealed interface Valid : ProjectDeserializationState {
                //TODO: Not used
                data class NewProject(val environmentsDirectory: ExistingDirectory) : Valid
                data class ExistingProject(val currentProject: Project) : Valid
            }
        }
    }

    sealed interface SetupSchema : CreateProjectState {
        val environmentName: NonEmptyString

        data class NewSchema(
            override val environmentName: NonEmptyString,
            val environmentsDirectory: ExistingDirectory,
            val globalSupportedPlatformTypes: NonEmptySet<PlatformType>? = null,
            val propertyDefinitions: List<PropertyDefinition> = emptyList(),
        ) : SetupSchema {
            fun emptyPropertyDefinition(): PropertyDefinition =
                PropertyDefinition(supportedPlatformTypes = globalSupportedPlatformTypes)

            data class PropertyDefinition(
                val name: NonEmptyString? = null,
                val propertyType: PropertyType = PropertyType.STRING,
                val nullable: Boolean = false,
                val supportedPlatformTypes: NonEmptySet<PlatformType>? = null,
            )
        }

        data class ExistingSchema(
            override val environmentName: NonEmptyString,
            val currentProject: Project,
        ) : SetupSchema
    }

    sealed interface SetupPlatforms : CreateProjectState {
        val environmentName: NonEmptyString

        data class NewProject(
            override val environmentName: NonEmptyString,
            val environmentsDirectory: ExistingDirectory,
            val schema: Schema,
            val platforms: NonEmptyKeySetStore<PlatformType, Platform>,
        ) : SetupPlatforms

        data class ExistingProject(
            override val environmentName: NonEmptyString,
            val currentProject: Project,
        ) : SetupPlatforms
    }
}