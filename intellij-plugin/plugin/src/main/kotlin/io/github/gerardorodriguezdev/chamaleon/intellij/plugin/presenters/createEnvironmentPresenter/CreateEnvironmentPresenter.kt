package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter

import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyType
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState.EnvironmentsDirectoryProcessResult
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState.Step
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.handlers.SetupEnvironmentHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.CoroutineContext

internal class CreateEnvironmentPresenter(
    private val uiScope: CoroutineScope,
    private val ioContext: CoroutineContext,

    private val projectDirectory: File,

    private val setupEnvironmentHandler: SetupEnvironmentHandler,

    private val onEnvironmentsDirectorySelected: () -> String?,
) {
    private val mutableStateFlow = MutableStateFlow<CreateEnvironmentState>(CreateEnvironmentState())
    val stateFlow: StateFlow<CreateEnvironmentState> = mutableStateFlow

    private var processJob: Job? = null

    fun onAction(action: CreateEnvironmentAction) {
        when (action) {
            is CreateEnvironmentAction.SetupEnvironmentAction -> action.handle()
            is CreateEnvironmentAction.SetupSchemaAction -> action.handle()
            is CreateEnvironmentAction.SetupPropertiesAction -> Unit //TODO: Finish
            is CreateEnvironmentAction.DialogAction -> action.handle()
        }
    }

    private fun CreateEnvironmentAction.SetupEnvironmentAction.handle() {
        when (this) {
            is CreateEnvironmentAction.SetupEnvironmentAction.OnInit -> process(projectDirectory)
            is CreateEnvironmentAction.SetupEnvironmentAction.OnSelectEnvironmentPathClicked -> handle()
            is CreateEnvironmentAction.SetupEnvironmentAction.OnEnvironmentNameChanged -> handle()
        }
    }

    private fun process(environmentsDirectory: File) {
        processJob?.cancel()

        processJob = uiScope.launch {
            setupEnvironmentHandler
                .handle(SetupEnvironmentHandler.Action.Process(environmentsDirectory))
                .flowOn(ioContext)
                .collect { sideEffect ->
                    when (sideEffect) {
                        is SetupEnvironmentHandler.SideEffect.UpdateEnvironmentsDirectoryState.Success -> {
                            mutableStateFlow.value = mutableStateFlow.value.copy(
                                environmentsDirectoryPath = sideEffect.environmentsDirectoryPath,
                                environmentsDirectoryProcessResult = EnvironmentsDirectoryProcessResult.Success,
                                environments = sideEffect.environments,
                                schema = sideEffect.schema,
                            )
                        }

                        is SetupEnvironmentHandler.SideEffect.UpdateEnvironmentsDirectoryState.Loading -> {
                            mutableStateFlow.value = mutableStateFlow.value.copy(
                                environmentsDirectoryPath = sideEffect.environmentsDirectoryPath,
                                environmentsDirectoryProcessResult = EnvironmentsDirectoryProcessResult.Loading,
                            )
                        }

                        is SetupEnvironmentHandler.SideEffect.UpdateEnvironmentsDirectoryState.Failure.FileIsNotDirectory -> {
                            mutableStateFlow.value = mutableStateFlow.value.copy(
                                environmentsDirectoryPath = sideEffect.environmentsDirectoryPath,
                                environmentsDirectoryProcessResult = EnvironmentsDirectoryProcessResult.Failure.FileIsNotDirectory,
                                environments = emptySet(),
                                schema = Schema.emptySchema(),
                            )
                        }

                        is SetupEnvironmentHandler.SideEffect.UpdateEnvironmentsDirectoryState.Failure.InvalidEnvironments -> {
                            mutableStateFlow.value = mutableStateFlow.value.copy(
                                environmentsDirectoryPath = sideEffect.environmentsDirectoryPath,
                                environmentsDirectoryProcessResult = EnvironmentsDirectoryProcessResult.Failure.InvalidEnvironments,
                                environments = emptySet(),
                                schema = Schema.emptySchema(),
                            )
                        }
                    }
                }
        }
    }

    //TODO: Common operations
    //TODO: Return actual selected property?
    private fun CreateEnvironmentAction.SetupSchemaAction.handle() {
        when (this) {
            is CreateEnvironmentAction.SetupSchemaAction.OnSupportedPlatformChanged -> {
                val currentSchema = mutableStateFlow.value.schema
                mutableStateFlow.value = mutableStateFlow.value.copy(
                    schema = currentSchema.copy(
                        supportedPlatforms = if (currentSchema.supportedPlatforms.contains(newPlatformType)) {
                            currentSchema.supportedPlatforms - newPlatformType
                        } else {
                            currentSchema.supportedPlatforms + newPlatformType
                        }
                    )
                )
            }

            is CreateEnvironmentAction.SetupSchemaAction.OnAddPropertyDefinitionClicked -> {
                val currentSchema = mutableStateFlow.value.schema
                mutableStateFlow.value = mutableStateFlow.value.copy(
                    schema = currentSchema.copy(
                        propertyDefinitions = currentSchema.propertyDefinitions + Schema.PropertyDefinition(
                            //TODO: Empty prop def
                            name = "",
                            propertyType = PropertyType.STRING,
                            nullable = true,
                            supportedPlatforms = currentSchema.supportedPlatforms,
                        )
                    )
                )
            }

            is CreateEnvironmentAction.SetupSchemaAction.OnPropertyNameChanged -> {
                val currentSchema = mutableStateFlow.value.schema
                mutableStateFlow.value = mutableStateFlow.value.copy(
                    schema = currentSchema.copy(
                        propertyDefinitions = currentSchema.propertyDefinitions.mapIndexed { currentIndex, propertyDefinition ->
                            if (index == currentIndex) {
                                propertyDefinition.copy(name = newName)
                            } else {
                                propertyDefinition
                            }
                        }.toSet()
                    )
                )
            }

            is CreateEnvironmentAction.SetupSchemaAction.OnPropertyTypeChanged -> {
                val currentSchema = mutableStateFlow.value.schema
                mutableStateFlow.value = mutableStateFlow.value.copy(
                    schema = currentSchema.copy(
                        propertyDefinitions = currentSchema.propertyDefinitions.mapIndexed { currentIndex, propertyDefinition ->
                            if (index == currentIndex) {
                                propertyDefinition.copy(
                                    propertyType = newPropertyType,
                                )
                            } else {
                                propertyDefinition
                            }
                        }.toSet()
                    )
                )
            }

            is CreateEnvironmentAction.SetupSchemaAction.OnNullableChanged -> {
                val currentSchema = mutableStateFlow.value.schema
                mutableStateFlow.value = mutableStateFlow.value.copy(
                    schema = currentSchema.copy(
                        propertyDefinitions = currentSchema.propertyDefinitions.mapIndexed { currentIndex, propertyDefinition ->
                            if (index == currentIndex) {
                                propertyDefinition.copy(nullable = newValue)
                            } else {
                                propertyDefinition
                            }
                        }.toSet()
                    )
                )
            }

            //TODO: Fix
            is CreateEnvironmentAction.SetupSchemaAction.OnPropertyDefinitionSupportedPlatformChanged -> {
                val currentSchema = mutableStateFlow.value.schema
                mutableStateFlow.value = mutableStateFlow.value.copy(
                    schema = currentSchema.copy(
                        propertyDefinitions = currentSchema.propertyDefinitions.mapIndexed { currentIndex, propertyDefinition ->
                            if (index == currentIndex) {
                                propertyDefinition.copy(
                                    supportedPlatforms = if (propertyDefinition.supportedPlatforms.contains(
                                            newPlatformType
                                        )
                                    ) {
                                        currentSchema.supportedPlatforms - newPlatformType
                                    } else {
                                        currentSchema.supportedPlatforms + newPlatformType
                                    }
                                )

                            } else {
                                propertyDefinition
                            }
                        }.toSet()
                    )
                )
            }
        }
    }

    private fun CreateEnvironmentAction.SetupEnvironmentAction.OnSelectEnvironmentPathClicked.handle() {
        val environmentsDirectoryPath = onEnvironmentsDirectorySelected() ?: return
        val environmentsDirectory = File(environmentsDirectoryPath)
        process(environmentsDirectory)
    }

    private fun CreateEnvironmentAction.SetupEnvironmentAction.OnEnvironmentNameChanged.handle() {
        mutableStateFlow.value = mutableStateFlow.value.copy(
            environmentName = newName,
        )
    }

    private fun CreateEnvironmentAction.DialogAction.handle() {
        when (this) {
            is CreateEnvironmentAction.DialogAction.OnPreviousButtonClicked -> handle()
            is CreateEnvironmentAction.DialogAction.OnNextButtonClicked -> handle()
            is CreateEnvironmentAction.DialogAction.OnFinishButtonClicked -> Unit //TODO: Finish
        }
    }

    private fun CreateEnvironmentAction.DialogAction.OnPreviousButtonClicked.handle() {
        when (mutableStateFlow.value.step) {
            Step.SETUP_ENVIRONMENT -> Unit
            Step.SETUP_SCHEMA -> mutableStateFlow.value = mutableStateFlow.value.copy(step = Step.SETUP_ENVIRONMENT)
        }
    }

    private fun CreateEnvironmentAction.DialogAction.OnNextButtonClicked.handle() {
        when (mutableStateFlow.value.step) {
            Step.SETUP_ENVIRONMENT -> mutableStateFlow.value = mutableStateFlow.value.copy(step = Step.SETUP_SCHEMA)
            Step.SETUP_SCHEMA -> Unit
        }
    }
}