package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter

import io.github.gerardorodriguezdev.chamaleon.core.models.*
import io.github.gerardorodriguezdev.chamaleon.core.models.Project.Companion.projectOf
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema.Companion.schemaOf
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectDeserializationResult
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectValidationResult
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore.Companion.toNonEmptyKeySetStore
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptySet.Companion.toUnsafeNonEmptySet
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString.Companion.toUnsafeNonEmptyString
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectDeserializer
import io.github.gerardorodriguezdev.chamaleon.core.utils.removeElementAtIndex
import io.github.gerardorodriguezdev.chamaleon.core.utils.updateElementByIndex
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.asDelegate
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.CreateProjectAction.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.CreateProjectState.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.CreateProjectState.SetupEnvironment.ProjectDeserializationState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateProjectPresenter(
    private val uiScope: CoroutineScope,
    private val ioScope: CoroutineScope,
    private val projectDeserializer: ProjectDeserializer,
    private val onFinish: (project: Project) -> Unit,
) {
    private val mutableStateFlow = MutableStateFlow<CreateProjectState>(SetupEnvironment())
    val stateFlow: StateFlow<CreateProjectState> = mutableStateFlow

    private var mutableState by mutableStateFlow.asDelegate()

    private var deserializeJob: Job? = null

    //TODO: Silent errors handling (null ? units noreturn)
    //TODO: Maybe states to know if can be navigated and how to do it
    fun dispatch(action: CreateProjectAction) {
        when (action) {
            is SetupEnvironmentAction -> {
                val currentState = mutableState.asSetupEnvironment()
                currentState?.let {
                    action.handle(currentState)
                }
            }

            is SetupSchemaAction -> {
                val currentState = mutableState.asSetupSchemaNewSchema()
                currentState?.let {
                    action.handle(currentState)
                }
            }

            is SetupPlatformsAction -> {
                val currentState = mutableState.asSetupPlatforms()
                currentState?.let {
                    action.handle(currentState)
                }
            }

            is NavigationAction -> action.handle()
        }
    }

    private fun SetupEnvironmentAction.handle(currentState: SetupEnvironment) =
        when (this) {
            is SetupEnvironmentAction.OnEnvironmentsDirectoryChanged -> handle(currentState)
            is SetupEnvironmentAction.OnEnvironmentNameChanged -> handle(currentState)
        }

    private fun SetupEnvironmentAction.OnEnvironmentsDirectoryChanged.handle(currentState: SetupEnvironment) {
        deserializeJob?.cancel()

        mutableState = currentState.copy(
            projectDeserializationState = ProjectDeserializationState.Loading(newEnvironmentsDirectory),
        )

        deserializeJob = uiScope
            .launch {
                withContext(ioScope.coroutineContext) {
                    val projectDeserializationResult = projectDeserializer.deserialize(newEnvironmentsDirectory)

                    withContext(uiScope.coroutineContext) {
                        when (projectDeserializationResult) {
                            is ProjectDeserializationResult.Success -> {
                                val newCurrentState = mutableState.asSetupEnvironment() ?: return@withContext

                                mutableState = newCurrentState.copy(
                                    projectDeserializationState = ProjectDeserializationState.Valid.ExistingProject(
                                        currentProject = projectDeserializationResult.project,
                                    )
                                )
                            }

                            is ProjectDeserializationResult.Failure -> {
                                val newCurrentState = mutableState.asSetupEnvironment() ?: return@withContext

                                mutableState = newCurrentState.copy(
                                    projectDeserializationState = ProjectDeserializationState.Invalid(
                                        environmentsDirectory = newEnvironmentsDirectory,
                                        errorMessage = "Invalid directory" //TODO: Lexemes here for all errors
                                    )
                                )
                            }
                        }
                    }
                }
            }
    }

    private fun SetupEnvironmentAction.OnEnvironmentNameChanged.handle(currentState: SetupEnvironment) {
        mutableState = currentState.copy(environmentName = newEnvironmentName)
    }

    private fun SetupSchemaAction.handle(currentState: SetupSchema.NewSchema) {
        when (this) {
            is SetupSchemaAction.OnGlobalSupportedPlatformTypesChanged -> handle(currentState)
            is SetupSchemaAction.OnAddPropertyDefinition -> handle(currentState)
            is SetupSchemaAction.OnPropertyDefinitionNameChanged -> handle(currentState)
            is SetupSchemaAction.OnDeletePropertyDefinition -> handle(currentState)
            is SetupSchemaAction.OnPropertyDefinitionTypeChanged -> handle(currentState)
            is SetupSchemaAction.OnNullableChanged -> handle(currentState)
            is SetupSchemaAction.OnSupportedPlatformTypesChanged -> handle(currentState)
        }
    }

    private fun SetupSchemaAction.OnGlobalSupportedPlatformTypesChanged.handle(currentState: SetupSchema.NewSchema) {
        val newGlobalSupportedPlatformTypes = if (isChecked) {
            if (currentState.globalSupportedPlatformTypes != null) {
                currentState.globalSupportedPlatformTypes.add(newPlatformType)
            } else {
                setOf(newPlatformType).toUnsafeNonEmptySet()
            }
        } else {
            currentState.globalSupportedPlatformTypes?.remove(newPlatformType)
        }

        val newPropertyDefinitions = currentState.propertyDefinitions.map { propertyDefinition ->
            propertyDefinition.copy(
                supportedPlatformTypes =
                    if (propertyDefinition.supportedPlatformTypes != null && newGlobalSupportedPlatformTypes != null) {
                        propertyDefinition.supportedPlatformTypes intersect newGlobalSupportedPlatformTypes
                    } else {
                        null
                    }
            )
        }

        mutableState = currentState.copy(
            globalSupportedPlatformTypes = newGlobalSupportedPlatformTypes,
            propertyDefinitions = newPropertyDefinitions,
        )
    }

    private fun SetupSchemaAction.OnAddPropertyDefinition.handle(currentState: SetupSchema.NewSchema) {
        mutableState = currentState.copy(
            propertyDefinitions = currentState.propertyDefinitions + currentState.emptyPropertyDefinition(),
        )
    }

    private fun SetupSchemaAction.OnPropertyDefinitionNameChanged.handle(currentState: SetupSchema.NewSchema) {
        mutableState = currentState.copy(
            propertyDefinitions = currentState.propertyDefinitions.updateElementByIndex(index) { propertyDefinition ->
                propertyDefinition.copy(name = newPropertyName)
            }
        )
    }

    private fun SetupSchemaAction.OnDeletePropertyDefinition.handle(currentState: SetupSchema.NewSchema) {
        mutableState = currentState.copy(
            propertyDefinitions = currentState.propertyDefinitions.removeElementAtIndex(index)
        )
    }

    private fun SetupSchemaAction.OnPropertyDefinitionTypeChanged.handle(currentState: SetupSchema.NewSchema) {
        mutableState = currentState.copy(
            propertyDefinitions = currentState.propertyDefinitions.updateElementByIndex(index) { propertyDefinition ->
                propertyDefinition.copy(propertyType = newPropertyType)
            }
        )
    }

    private fun SetupSchemaAction.OnNullableChanged.handle(currentState: SetupSchema.NewSchema) {
        mutableState = currentState.copy(
            propertyDefinitions = currentState.propertyDefinitions.updateElementByIndex(index) { propertyDefinition ->
                propertyDefinition.copy(nullable = newNullable)
            }
        )
    }

    private fun SetupSchemaAction.OnSupportedPlatformTypesChanged.handle(currentState: SetupSchema.NewSchema) {
        mutableState = currentState.copy(
            propertyDefinitions = currentState.propertyDefinitions.updateElementByIndex(index) { propertyDefinition ->
                propertyDefinition.copy(
                    supportedPlatformTypes = if (isChecked) {
                        if (currentState.globalSupportedPlatformTypes?.contains(newPlatformType) == true) {
                            propertyDefinition.supportedPlatformTypes?.add(newPlatformType)
                        } else {
                            propertyDefinition.supportedPlatformTypes
                        }
                    } else {
                        propertyDefinition.supportedPlatformTypes?.remove(newPlatformType)
                    }
                )
            }
        )
    }

    private fun SetupPlatformsAction.handle(currentState: SetupPlatforms) =
        when (this) {
            is SetupPlatformsAction.OnPropertyValueChanged -> when (currentState) {
                is SetupPlatforms.NewProject -> handle(currentState)
                is SetupPlatforms.ExistingProject -> handle(currentState)
            }
        }

    private fun SetupPlatformsAction.OnPropertyValueChanged.handle(currentState: SetupPlatforms.NewProject) {
        val platform = currentState.platforms.getValue(platformType)

        mutableState = currentState.copy(
            platforms = currentState.platforms.updateElementByKey(
                newValue = platform.copy(
                    properties = platform.properties.updateElementByIndex(index) { property ->
                        property.copy(value = newPropertyValue)
                    }
                )
            )
        )
    }

    private fun SetupPlatformsAction.OnPropertyValueChanged.handle(currentState: SetupPlatforms.ExistingProject) {
        val environment = currentState.currentProject.environments?.getValue(currentState.environmentName.value)
        val platform = environment?.platforms?.getValue(platformType) ?: return

        val newEnvironment = environment.copy(
            platforms = environment.platforms.updateElementByKey(
                newValue = platform.copy(
                    properties = platform.properties.updateElementByIndex(index) { property ->
                        property.copy(
                            value = newPropertyValue,
                        )
                    }
                )
            )
        )

        val newProject = currentState.currentProject.updateEnvironment(newEnvironment) ?: return

        mutableState = currentState.copy(currentProject = newProject)
    }

    private fun NavigationAction.handle() {
        when (this) {
            is NavigationAction.OnPrevious -> handle()
            is NavigationAction.OnNext -> handle()
            is NavigationAction.OnFinish -> handle()
        }
    }

    private fun NavigationAction.OnPrevious.handle() {
        val currentState = mutableState
        when (currentState) {
            is SetupEnvironment -> Unit
            is SetupSchema -> mutableState = currentState.toSetupEnvironment()
            is SetupPlatforms -> mutableState = currentState.toSetupSchema()
        }
    }

    private fun SetupSchema.toSetupEnvironment(): SetupEnvironment =
        SetupEnvironment(
            environmentName = environmentName,
            projectDeserializationState = when (this) {
                is SetupSchema.NewSchema -> ProjectDeserializationState.Valid.NewProject(environmentsDirectory)
                is SetupSchema.ExistingSchema -> ProjectDeserializationState.Valid.ExistingProject(currentProject)
            },
        )

    private fun SetupPlatforms.toSetupSchema(): SetupSchema =
        when (this) {
            is SetupPlatforms.NewProject -> SetupSchema.NewSchema(
                environmentName = environmentName,
                environmentsDirectory = environmentsDirectory,
                globalSupportedPlatformTypes = schema.globalSupportedPlatformTypes,
                propertyDefinitions = schema.propertyDefinitions.values.map { propertyDefinition ->
                    propertyDefinition.toSetupSchemaPropertyDefinition()
                },
            )

            is SetupPlatforms.ExistingProject -> SetupSchema.ExistingSchema(
                environmentName = environmentName,
                currentProject = currentProject,
            )
        }

    private fun NavigationAction.OnNext.handle() {
        val currentState = mutableState
        when (currentState) {
            is SetupEnvironment -> {
                val setupSchema = currentState.toSetupSchema()
                setupSchema?.let {
                    mutableState = setupSchema
                }
            }

            is SetupSchema -> {
                val setupPlatforms = currentState.toSetupPlatforms()
                setupPlatforms?.let {
                    mutableState = setupPlatforms
                }
            }

            is SetupPlatforms -> Unit
        }
    }

    private fun SetupEnvironment.toSetupSchema(): SetupSchema? {
        val projectDeserializationState = projectDeserializationState ?: return null
        return when (projectDeserializationState) {
            is ProjectDeserializationState.Valid.NewProject -> SetupSchema.NewSchema(
                environmentName = environmentName ?: return null,
                environmentsDirectory = projectDeserializationState.environmentsDirectory,
            )

            is ProjectDeserializationState.Valid.ExistingProject -> SetupSchema.ExistingSchema(
                environmentName = environmentName ?: return null,
                currentProject = projectDeserializationState.currentProject,
            )

            is ProjectDeserializationState.Invalid -> null
            is ProjectDeserializationState.Loading -> null
        }
    }

    private fun Schema.PropertyDefinition.toSetupSchemaPropertyDefinition(): SetupSchema.NewSchema.PropertyDefinition =
        SetupSchema.NewSchema.PropertyDefinition(
            name = name,
            propertyType = propertyType,
            nullable = nullable,
            supportedPlatformTypes = supportedPlatformTypes,
        )

    private fun SetupSchema.toSetupPlatforms(): SetupPlatforms? {
        return when (this) {
            is SetupSchema.NewSchema -> toSetupPlatformsNewProject()
            is SetupSchema.ExistingSchema -> SetupPlatforms.ExistingProject(
                environmentName = environmentName,
                currentProject = currentProject,
            )
        }
    }

    private fun SetupSchema.NewSchema.toSetupPlatformsNewProject(): SetupPlatforms.NewProject? {
        val schema = toSchema() ?: return null

        return SetupPlatforms.NewProject(
            environmentName = environmentName,
            environmentsDirectory = environmentsDirectory,
            schema = schema,
            platforms = schema.toEmptyPlatforms() ?: return null,
        )
    }

    private fun SetupSchema.NewSchema.toSchema(): Schema? {
        return schemaOf(
            globalSupportedPlatformTypes = globalSupportedPlatformTypes ?: return null,
            propertyDefinitions = propertyDefinitions.map { propertyDefinition ->
                propertyDefinition.toSchemaPropertyDefinition() ?: return null
            }.toNonEmptyKeySetStore() ?: return null,
        )
    }

    private fun SetupSchema.NewSchema.PropertyDefinition.toSchemaPropertyDefinition(): Schema.PropertyDefinition? {
        return Schema.PropertyDefinition(
            name = name ?: return null,
            propertyType = propertyType,
            nullable = nullable,
            supportedPlatformTypes = supportedPlatformTypes ?: return null,
        )
    }

    private fun Schema.toEmptyPlatforms(): NonEmptyKeySetStore<PlatformType, Platform>? {
        return globalSupportedPlatformTypes.map { globalSupportedPlatformType ->
            Platform(
                platformType = globalSupportedPlatformType,
                properties = propertyDefinitions.values.map { propertyDefinition ->
                    propertyDefinition.toEmptyProperty()
                }.toNonEmptyKeySetStore() ?: return null,
            )
        }.toNonEmptyKeySetStore()
    }

    private fun Schema.PropertyDefinition.toEmptyProperty(): Platform.Property =
        Platform.Property(
            name = name,
            value = when (propertyType) {
                PropertyType.STRING -> PropertyValue.StringProperty("value".toUnsafeNonEmptyString())
                PropertyType.BOOLEAN -> PropertyValue.BooleanProperty(false)
            },
        )

    private fun NavigationAction.OnFinish.handle() {
        val currentState = mutableState
        when (currentState) {
            is SetupEnvironment -> Unit
            is SetupSchema -> Unit
            is SetupPlatforms -> when (currentState) {
                is SetupPlatforms.NewProject -> currentState.onFinish()
                is SetupPlatforms.ExistingProject -> onFinish(currentState.currentProject)
            }
        }
    }

    private fun SetupPlatforms.NewProject.onFinish() {
        val projectValidationResult = projectOf(
            environmentsDirectory = environmentsDirectory,
            schema = schema,
            properties = Properties(),
            environments = setOf(
                Environment(
                    name = environmentName,
                    platforms = platforms,
                ),
            ).toNonEmptyKeySetStore(),
        )

        when (projectValidationResult) {
            is ProjectValidationResult.Success -> onFinish(projectValidationResult.project)
            is ProjectValidationResult.Failure -> Unit
        }
    }
}