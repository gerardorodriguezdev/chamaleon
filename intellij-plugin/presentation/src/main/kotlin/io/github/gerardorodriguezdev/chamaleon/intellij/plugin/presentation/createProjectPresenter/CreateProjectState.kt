package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter

import io.github.gerardorodriguezdev.chamaleon.core.models.*
import io.github.gerardorodriguezdev.chamaleon.core.models.Project.Companion.isValidProject
import io.github.gerardorodriguezdev.chamaleon.core.models.Project.Companion.projectOf
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema.Companion.schemaOf
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory.Companion.toExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore.Companion.toNonEmptyKeySetStore
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptySet
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString.Companion.toUnsafeNonEmptyString
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.CreateProjectState.SetupEnvironment.ProjectDeserializationState

sealed interface CreateProjectState {
    fun asSetupEnvironment(): SetupEnvironment? = this as? SetupEnvironment
    fun asSetupSchemaNewSchema(): SetupSchema.NewSchema? = this as? SetupSchema.NewSchema
    fun asSetupPlatforms(): SetupPlatforms? = this as? SetupPlatforms

    fun toPrevious(): CreateProjectState?
    fun toNext(): CreateProjectState?
    fun canFinish(): Boolean
    fun toFinish(): Finish?

    data class SetupEnvironment(
        val projectDeserializationState: ProjectDeserializationState? = null,
        val environmentName: NonEmptyString? = null,
    ) : CreateProjectState {

        override fun toPrevious(): CreateProjectState? = null

        override fun toNext(): CreateProjectState? {
            return when (projectDeserializationState) {
                null -> null
                is ProjectDeserializationState.Valid.NewProject -> SetupSchema.NewSchema(
                    environmentName = environmentName ?: return null,
                    environmentsDirectoryPath = projectDeserializationState.environmentsDirectoryPath,
                )

                is ProjectDeserializationState.Valid.ExistingProject -> SetupSchema.ExistingSchema(
                    environmentName = environmentName ?: return null,
                    currentProject = projectDeserializationState.currentProject,
                )

                is ProjectDeserializationState.Invalid -> null
                is ProjectDeserializationState.Loading -> null
            }
        }

        override fun canFinish(): Boolean = false

        override fun toFinish(): Finish? = null

        sealed interface ProjectDeserializationState {
            data class Loading(
                val environmentsDirectoryPath: NonEmptyString,
            ) : ProjectDeserializationState

            data class Invalid(
                val environmentsDirectoryPath: NonEmptyString,
                val errorMessage: String
            ) : ProjectDeserializationState

            sealed interface Valid : ProjectDeserializationState {
                data class NewProject(val environmentsDirectoryPath: NonEmptyString) : Valid
                data class ExistingProject(val currentProject: Project) : Valid
            }
        }
    }

    sealed interface SetupSchema : CreateProjectState {
        val environmentName: NonEmptyString

        override fun toPrevious(): CreateProjectState? =
            SetupEnvironment(
                environmentName = environmentName,
                projectDeserializationState = when (this) {
                    is NewSchema -> ProjectDeserializationState.Valid.NewProject(environmentsDirectoryPath)
                    is ExistingSchema -> ProjectDeserializationState.Valid.ExistingProject(currentProject)
                },
            )

        override fun canFinish(): Boolean = false

        override fun toFinish(): Finish? = null

        fun Schema.toEmptyPlatforms(): NonEmptyKeySetStore<PlatformType, Platform> {
            return globalSupportedPlatformTypes.mapToNonEmptyKeySetStore { globalSupportedPlatformType ->
                Platform(
                    platformType = globalSupportedPlatformType,
                    properties = propertyDefinitions.mapToNonEmptyKeySetStore { propertyDefinition ->
                        propertyDefinition.toEmptyProperty()
                    },
                )
            }
        }

        private fun Schema.PropertyDefinition.toEmptyProperty(): Platform.Property =
            Platform.Property(
                name = name,
                value = when (propertyType) {
                    PropertyType.STRING -> PropertyValue.StringProperty("value".toUnsafeNonEmptyString())
                    PropertyType.BOOLEAN -> PropertyValue.BooleanProperty(false)
                },
            )

        data class NewSchema(
            override val environmentName: NonEmptyString,
            val environmentsDirectoryPath: NonEmptyString,
            val globalSupportedPlatformTypes: NonEmptySet<PlatformType>? = null,
            val propertyDefinitions: List<PropertyDefinition> = emptyList(),
        ) : SetupSchema {
            override fun toNext(): CreateProjectState? {
                val schema = toSchema() ?: return null

                return SetupPlatforms.NewProject(
                    environmentName = environmentName,
                    environmentsDirectoryPath = environmentsDirectoryPath,
                    schema = schema,
                    platforms = schema.toEmptyPlatforms(),
                )
            }

            private fun toSchema(): Schema? {
                return schemaOf(
                    globalSupportedPlatformTypes = globalSupportedPlatformTypes ?: return null,
                    propertyDefinitions = propertyDefinitions.map { propertyDefinition ->
                        propertyDefinition.toSchemaPropertyDefinition() ?: return null
                    }.toNonEmptyKeySetStore() ?: return null,
                ).schema()
            }

            fun emptyPropertyDefinition(): PropertyDefinition =
                PropertyDefinition(supportedPlatformTypes = globalSupportedPlatformTypes)

            data class PropertyDefinition(
                val name: NonEmptyString? = null,
                val propertyType: PropertyType = PropertyType.STRING,
                val nullable: Boolean = false,
                val supportedPlatformTypes: NonEmptySet<PlatformType>? = null,
            ) {
                fun toSchemaPropertyDefinition(): Schema.PropertyDefinition? {
                    return Schema.PropertyDefinition(
                        name = name ?: return null,
                        propertyType = propertyType,
                        nullable = nullable,
                        supportedPlatformTypes = supportedPlatformTypes ?: return null,
                    )
                }
            }
        }

        data class ExistingSchema(
            override val environmentName: NonEmptyString,
            val currentProject: Project,
        ) : SetupSchema {
            override fun toNext(): CreateProjectState? =
                SetupPlatforms.ExistingProject(
                    environmentName = environmentName,
                    currentProject = currentProject,
                    platforms = currentProject.schema.toEmptyPlatforms(),
                )
        }
    }

    sealed interface SetupPlatforms : CreateProjectState {
        val environmentName: NonEmptyString
        val platforms: NonEmptyKeySetStore<PlatformType, Platform>

        override fun toNext(): CreateProjectState? = null

        data class NewProject(
            override val environmentName: NonEmptyString,
            val environmentsDirectoryPath: NonEmptyString,
            val schema: Schema,
            override val platforms: NonEmptyKeySetStore<PlatformType, Platform>,
        ) : SetupPlatforms {
            override fun toPrevious(): CreateProjectState? =
                SetupSchema.NewSchema(
                    environmentName = environmentName,
                    environmentsDirectoryPath = environmentsDirectoryPath,
                    globalSupportedPlatformTypes = schema.globalSupportedPlatformTypes,
                    propertyDefinitions = schema.propertyDefinitions.values.map { propertyDefinition ->
                        propertyDefinition.toSetupSchemaPropertyDefinition()
                    },
                )

            override fun canFinish(): Boolean =
                isValidProject(
                    environmentsDirectoryPath = environmentsDirectoryPath,
                    schema = schema,
                    properties = Properties(),
                    environments = setOf(
                        Environment(
                            name = environmentName,
                            platforms = platforms,
                        ),
                    ).toNonEmptyKeySetStore(),
                )

            override fun toFinish(): Finish? {
                val environmentsDirectory =
                    environmentsDirectoryPath.value.toExistingDirectory(createIfNotPresent = true) ?: return null

                val project = projectOf(
                    environmentsDirectory = environmentsDirectory,
                    schema = schema,
                    properties = Properties(),
                    environments = setOf(
                        Environment(
                            name = environmentName,
                            platforms = platforms,
                        ),
                    ).toNonEmptyKeySetStore(),
                ).project()

                return project?.let {
                    Finish(project)
                }
            }

            private fun Schema.PropertyDefinition.toSetupSchemaPropertyDefinition(): SetupSchema.NewSchema.PropertyDefinition =
                SetupSchema.NewSchema.PropertyDefinition(
                    name = name,
                    propertyType = propertyType,
                    nullable = nullable,
                    supportedPlatformTypes = supportedPlatformTypes,
                )
        }

        data class ExistingProject(
            override val environmentName: NonEmptyString,
            val currentProject: Project,
            override val platforms: NonEmptyKeySetStore<PlatformType, Platform>,
        ) : SetupPlatforms {
            override fun toPrevious(): CreateProjectState? =
                SetupSchema.ExistingSchema(
                    environmentName = environmentName,
                    currentProject = currentProject,
                )

            override fun canFinish(): Boolean = toFinish() != null

            override fun toFinish(): Finish? = Finish(currentProject)
        }
    }

    data class Finish(val project: Project) : CreateProjectState {
        override fun toPrevious(): CreateProjectState? = null
        override fun toNext(): CreateProjectState? = null
        override fun canFinish(): Boolean = false
        override fun toFinish(): Finish? = null
    }
}