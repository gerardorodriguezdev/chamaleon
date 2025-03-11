package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter

import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectDeserializationResult
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptySet.Companion.toUnsafeNonEmptySet
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
) {
    private val mutableStateFlow = MutableStateFlow<CreateProjectState>(SetupEnvironment())
    val stateFlow: StateFlow<CreateProjectState> = mutableStateFlow

    private var mutableState by mutableStateFlow.asDelegate()

    private var deserializeJob: Job? = null

    //TODO: Silent errors handling (null ? units noreturn)
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

        //TODO: Here full error
        val newProject = currentState.currentProject.updateEnvironment(newEnvironment) ?: return

        mutableState = currentState.copy(currentProject = newProject)
    }

    private fun NavigationAction.handle() {
        when (this) {
            is NavigationAction.OnPrevious -> {
                val newState = mutableState.toPrevious()
                newState?.let {
                    mutableState = newState
                }
            }

            is NavigationAction.OnNext -> {
                val newState = mutableState.toNext()
                newState?.let {
                    mutableState = newState
                }
            }

            is NavigationAction.OnFinish -> {
                val project = mutableState.toFinish()
                project?.let {
                    mutableState = project
                }
            }
        }
    }
}