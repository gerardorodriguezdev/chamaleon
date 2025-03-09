package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter

import io.github.gerardorodriguezdev.chamaleon.core.models.Project
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectDeserializationResult
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptySet.Companion.toNonEmptySet
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptySet.Companion.toUnsafeNonEmptySet
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectDeserializer
import io.github.gerardorodriguezdev.chamaleon.core.utils.removeElementAtIndex
import io.github.gerardorodriguezdev.chamaleon.core.utils.updateElementByIndex
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.asDelegate
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.CreateProjectAction.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.CreateProjectState.SetupEnvironment
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.CreateProjectState.SetupEnvironment.ProjectDeserializationState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.CreateProjectState.SetupSchema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

//TODO: Refactor a bit functions
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
        mutableState = mutableState.asSetupEnvironment().copy(
            environmentName = newEnvironmentName,
        )
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
            setupSchema.globalSupportedPlatformTypes?.add(newPlatformType)
                ?: setOf(newPlatformType).toUnsafeNonEmptySet()
        } else {
            setupSchema.globalSupportedPlatformTypes?.remove(newPlatformType)
        }

        val newPropertyDefinitions = setupSchema.propertyDefinitions.map { propertyDefinition ->
            propertyDefinition.copy(
                supportedPlatformTypes = if (propertyDefinition.supportedPlatformTypes != null && newGlobalSupportedPlatformTypes != null) {
                    (propertyDefinition.supportedPlatformTypes.value intersect newGlobalSupportedPlatformTypes.value).toUnsafeNonEmptySet()
                } else {
                    null
                }
            )
        }
        mutableState = setupSchema.copy(
            globalSupportedPlatformTypes =
                setupSchema.globalSupportedPlatformTypes?.add(newPlatformType)
                    ?: setOf(newPlatformType).toNonEmptySet(),
            propertyDefinitions = newPropertyDefinitions,
        )
    }

    private fun SetupSchemaAction.OnAddPropertyDefinition.handle() {
        val setupSchema = mutableState.asSetupSchema()
        mutableState = setupSchema.copy(
            propertyDefinitions = setupSchema.propertyDefinitions + SetupSchema.PropertyDefinition(
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
                        properties = platform
                            .properties
                            .updateElementByIndex(index) { property ->
                                property.copy(value = newPropertyValue)
                            }
                    )
                } else {
                    platform
                }
            }
        )
    }

    private fun NavigationAction.handle() =
        when (this) {
            is NavigationAction.Previous -> Unit
            is NavigationAction.Next -> Unit
            is NavigationAction.Finish -> Unit
        }
}