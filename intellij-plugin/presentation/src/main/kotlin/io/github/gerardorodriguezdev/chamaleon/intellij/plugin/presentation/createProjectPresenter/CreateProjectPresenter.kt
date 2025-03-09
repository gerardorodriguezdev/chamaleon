package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter

import io.github.gerardorodriguezdev.chamaleon.core.models.*
import io.github.gerardorodriguezdev.chamaleon.core.models.Project.Companion.projectOf
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema.Companion.schemaOf
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectDeserializationResult
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectValidationResult
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore.Companion.toNonEmptyKeySetStore
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore.Companion.toUnsafeNonEmptyKeyStore
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
import kotlin.coroutines.CoroutineContext

internal class CreateProjectPresenter(
    private val uiScope: CoroutineScope,
    private val uiContext: CoroutineContext,
    private val ioContext: CoroutineContext,
    private val projectDeserializer: ProjectDeserializer,
    private val onFinish: (project: Project) -> Unit,
) {
    private val mutableStateFlow = MutableStateFlow<CreateProjectState>(SetupEnvironment())
    val stateFlow: StateFlow<CreateProjectState> = mutableStateFlow

    private var mutableState by mutableStateFlow.asDelegate()

    private var deserializeJob: Job? = null

    fun dispatch(action: CreateProjectAction) {
        when (action) {
            is SetupEnvironmentAction -> action.handle()
            is SetupSchemaAction -> action.handle()
            is SetupPropertiesAction -> action.handle()
            is NavigationAction -> action.handle()
        }
    }

    private fun SetupEnvironmentAction.handle() =
        when (this) {
            is SetupEnvironmentAction.OnEnvironmentsDirectoryChanged -> handle()
            is SetupEnvironmentAction.OnEnvironmentNameChanged -> handle()
        }

    //TODO: Better way to pass the correct state each time?
    //TODO: Mutate state functions each?
    private fun SetupEnvironmentAction.OnEnvironmentsDirectoryChanged.handle() {
        deserializeJob?.cancel()

        mutableState = mutableState.asSetupEnvironment().copy(
            projectDeserializationState = ProjectDeserializationState.Loading(newEnvironmentsDirectory),
        )

        deserializeJob = uiScope
            .launch {
                withContext(ioContext) {
                    val projectDeserializationResult = projectDeserializer.deserialize(newEnvironmentsDirectory)

                    withContext(uiContext) {
                        when (projectDeserializationResult) {
                            is ProjectDeserializationResult.Success -> {
                                mutableState = mutableState.asSetupEnvironment().copy(
                                    projectDeserializationState = ProjectDeserializationState.Valid(
                                        environmentsDirectory = newEnvironmentsDirectory,
                                        project = projectDeserializationResult.project,
                                    )
                                )
                            }

                            is ProjectDeserializationResult.Failure -> {
                                mutableState = mutableState.asSetupEnvironment().copy(
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

    private fun SetupEnvironmentAction.OnEnvironmentNameChanged.handle() {
        mutableState = mutableState.asSetupEnvironment().copy(environmentName = newEnvironmentName)
    }

    private fun SetupSchemaAction.handle() {
        when (this) {
            is SetupSchemaAction.OnGlobalSupportedPlatformTypesChanged -> handle()
            is SetupSchemaAction.OnAddPropertyDefinition -> handle()
            is SetupSchemaAction.OnPropertyDefinitionNameChanged -> handle()
            is SetupSchemaAction.OnDeletePropertyDefinition -> handle()
            is SetupSchemaAction.OnPropertyDefinitionTypeChanged -> handle()
            is SetupSchemaAction.OnNullableChanged -> handle()
            is SetupSchemaAction.OnSupportedPlatformTypesChanged -> handle()
        }
    }

    private fun SetupSchemaAction.OnGlobalSupportedPlatformTypesChanged.handle() {
        val setupSchema = mutableState.asSetupSchema()

        val newGlobalSupportedPlatformTypes = if (isChecked) {
            if (setupSchema.globalSupportedPlatformTypes != null) {
                setupSchema.globalSupportedPlatformTypes.add(newPlatformType)
            } else {
                setOf(newPlatformType).toUnsafeNonEmptySet()
            }
        } else {
            setupSchema.globalSupportedPlatformTypes?.remove(newPlatformType)
        }

        val newPropertyDefinitions = setupSchema.propertyDefinitions.map { propertyDefinition ->
            propertyDefinition.copy(
                supportedPlatformTypes =
                    if (propertyDefinition.supportedPlatformTypes != null && newGlobalSupportedPlatformTypes != null) {
                        (propertyDefinition.supportedPlatformTypes.value intersect newGlobalSupportedPlatformTypes.value).toUnsafeNonEmptySet() //TODO: To safe intersect fun
                    } else {
                        null
                    }
            )
        }

        mutableState = setupSchema.copy(
            globalSupportedPlatformTypes = newGlobalSupportedPlatformTypes,
            propertyDefinitions = newPropertyDefinitions,
        )
    }

    private fun SetupSchemaAction.OnAddPropertyDefinition.handle() {
        val setupSchema = mutableState.asSetupSchema()
        mutableState = setupSchema.copy(
            propertyDefinitions = setupSchema.propertyDefinitions + SetupSchema.PropertyDefinition(
                //TODO: Maybe function
                supportedPlatformTypes = setupSchema.globalSupportedPlatformTypes,
            ),
        )
    }

    private fun SetupSchemaAction.OnPropertyDefinitionNameChanged.handle() {
        val setupSchema = mutableState.asSetupSchema()
        mutableState = setupSchema.copy(
            propertyDefinitions = setupSchema.propertyDefinitions.updateElementByIndex(index) { propertyDefinition ->
                propertyDefinition.copy(name = newPropertyName)
            }
        )
    }

    private fun SetupSchemaAction.OnDeletePropertyDefinition.handle() {
        val setupSchema = mutableState.asSetupSchema()
        mutableState = setupSchema.copy(
            propertyDefinitions = setupSchema.propertyDefinitions.removeElementAtIndex(index)
        )
    }

    private fun SetupSchemaAction.OnPropertyDefinitionTypeChanged.handle() {
        val setupSchema = mutableState.asSetupSchema()
        mutableState = setupSchema.copy(
            propertyDefinitions = setupSchema.propertyDefinitions.updateElementByIndex(index) { propertyDefinition ->
                propertyDefinition.copy(propertyType = newPropertyType)
            }
        )
    }

    private fun SetupSchemaAction.OnNullableChanged.handle() {
        val setupSchema = mutableState.asSetupSchema()
        mutableState = setupSchema.copy(
            propertyDefinitions = setupSchema.propertyDefinitions.updateElementByIndex(index) { propertyDefinition ->
                propertyDefinition.copy(nullable = newNullable)
            }
        )
    }

    private fun SetupSchemaAction.OnSupportedPlatformTypesChanged.handle() {
        val setupSchema = mutableState.asSetupSchema()
        mutableState = setupSchema.copy(
            propertyDefinitions = setupSchema.propertyDefinitions.updateElementByIndex(index) { propertyDefinition ->
                propertyDefinition.copy(
                    supportedPlatformTypes = if (isChecked) {
                        if (setupSchema.globalSupportedPlatformTypes?.contains(newPlatformType) == true) {
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

    private fun SetupPropertiesAction.handle() =
        when (this) {
            is SetupPropertiesAction.OnPropertyValueChanged -> handle()
        }

    private fun SetupPropertiesAction.OnPropertyValueChanged.handle() {
        val setupPlatforms = mutableState.asSetupPlatforms()
        mutableState = setupPlatforms.copy(
            platforms = setupPlatforms.platforms.updateElementByIndex(index) { platform ->
                if (platform.platformType == platformType) {
                    platform.copy(
                        properties = platform.properties.updateElementByIndex(index) { property ->
                            property.copy(value = newPropertyValue)
                        }
                    )
                } else {
                    platform
                }
            }
        )
    }

    // TODO: Refactor (sep in different action handling + how to get correct next state on each?)
    private fun NavigationAction.handle() =
        when (this) {
            is NavigationAction.Previous -> {
                val currentState = mutableState
                when (currentState) {
                    is SetupEnvironment -> Unit
                    is SetupSchema -> {
                        mutableState = SetupEnvironment(
                            projectDeserializationState = ProjectDeserializationState.Valid(
                                environmentsDirectory = currentState.environmentsDirectory,
                                project = currentState.currentProject,
                            ),
                            environmentName = currentState.environmentName,
                        )
                    }

                    is SetupPlatforms -> {
                        mutableState = SetupSchema(
                            environmentsDirectory = currentState.environmentsDirectory,
                            environmentName = currentState.environmentName,
                            currentProject = currentState.currentProject,
                            globalSupportedPlatformTypes = currentState.schema.globalSupportedPlatformTypes,
                            propertyDefinitions = currentState.schema.propertyDefinitions.values.map { propertyDefinition ->
                                propertyDefinition.toSetupSchemaPropertyDefinition()
                            },
                        )
                    }
                }
            }

            is NavigationAction.Next -> {
                val currentState = mutableState
                when (currentState) {
                    is SetupEnvironment -> {
                        val projectDeserializationState =
                            currentState.projectDeserializationState as ProjectDeserializationState.Valid

                        mutableState = SetupSchema(
                            environmentsDirectory = projectDeserializationState.environmentsDirectory,
                            environmentName = requireNotNull(currentState.environmentName),
                            currentProject = projectDeserializationState.project,
                        )
                    }

                    is SetupSchema -> {
                        val schema = requireNotNull(
                            schemaOf(
                                globalSupportedPlatformTypes = requireNotNull(currentState.globalSupportedPlatformTypes),
                                propertyDefinitions = currentState.propertyDefinitions.map { propertyDefinition ->
                                    propertyDefinition.toSchemaPropertyDefinition()
                                }.toUnsafeNonEmptyKeyStore(),
                            )
                        )
                        mutableState = SetupPlatforms(
                            environmentsDirectory = currentState.environmentsDirectory,
                            environmentName = currentState.environmentName,
                            currentProject = currentState.currentProject,
                            schema = schema,
                            platforms = requireNotNull(
                                schema.globalSupportedPlatformTypes.map { globalSupportedPlatformType ->
                                    Platform(
                                        platformType = globalSupportedPlatformType,
                                        properties = requireNotNull(
                                            schema.propertyDefinitions.values.map { propertyDefinition ->
                                                Platform.Property(
                                                    name = propertyDefinition.name,
                                                    value = when (propertyDefinition.propertyType) {
                                                        PropertyType.STRING -> PropertyValue.StringProperty("value".toUnsafeNonEmptyString())
                                                        PropertyType.BOOLEAN -> PropertyValue.BooleanProperty(false)
                                                    },
                                                )
                                            }.toNonEmptyKeySetStore()
                                        )
                                    )
                                }.toNonEmptyKeySetStore()
                            )
                        )
                    }

                    is SetupPlatforms -> Unit
                }
            }

            is NavigationAction.Finish -> {
                val currentState = mutableState
                when (currentState) {
                    is SetupEnvironment -> Unit
                    is SetupSchema -> Unit
                    is SetupPlatforms -> {
                        val newEnvironments = setOf(
                            Environment(
                                name = currentState.environmentName,
                                platforms = currentState.platforms,
                            )
                        ).toUnsafeNonEmptyKeyStore()

                        val project = if (currentState.currentProject != null) {
                            requireNotNull(currentState.currentProject.addEnvironments(newEnvironments))
                        } else {
                            val projectValidationResult = projectOf(
                                environmentsDirectory = currentState.environmentsDirectory,
                                schema = currentState.schema,
                                properties = Properties(),
                                environments = newEnvironments,
                            ) as ProjectValidationResult.Success
                            projectValidationResult.project
                        }

                        onFinish(project)
                    }
                }
            }
        }

    private fun SetupSchema.PropertyDefinition.toSchemaPropertyDefinition(): Schema.PropertyDefinition =
        Schema.PropertyDefinition(
            name = requireNotNull(name),
            propertyType = propertyType,
            nullable = nullable,
            supportedPlatformTypes = requireNotNull(supportedPlatformTypes),
        )

    private fun Schema.PropertyDefinition.toSetupSchemaPropertyDefinition(): SetupSchema.PropertyDefinition =
        SetupSchema.PropertyDefinition(
            name = name,
            propertyType = propertyType,
            nullable = nullable,
            supportedPlatformTypes = supportedPlatformTypes,
        )
}