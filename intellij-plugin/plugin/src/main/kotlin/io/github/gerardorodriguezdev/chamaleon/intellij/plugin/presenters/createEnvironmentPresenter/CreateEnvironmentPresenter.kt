package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter

import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyType
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema.PropertyDefinition
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.asDelegate
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

    private var mutableState by mutableStateFlow.asDelegate()

    private var processJob: Job? = null

    fun onAction(action: CreateEnvironmentAction) {
        when (action) {
            is CreateEnvironmentAction.SetupEnvironmentAction -> action.handle()
            is CreateEnvironmentAction.SetupSchemaAction -> action.handle()
            is CreateEnvironmentAction.SetupPropertiesAction -> Unit //TODO: Finish
            is CreateEnvironmentAction.DialogAction -> action.handle()
        }
    }

    private fun CreateEnvironmentAction.SetupEnvironmentAction.handle() =
        when (this) {
            is CreateEnvironmentAction.SetupEnvironmentAction.OnInit -> process(projectDirectory)
            is CreateEnvironmentAction.SetupEnvironmentAction.OnSelectEnvironmentPathClicked -> handle()
            is CreateEnvironmentAction.SetupEnvironmentAction.OnEnvironmentNameChanged -> handle()
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
                            mutableState = mutableState.copy(
                                environmentsDirectoryPath = sideEffect.environmentsDirectoryPath,
                                environmentsDirectoryProcessResult = EnvironmentsDirectoryProcessResult.Success,
                                environments = sideEffect.environments,
                                schema = sideEffect.schema,
                            )
                        }

                        is SetupEnvironmentHandler.SideEffect.UpdateEnvironmentsDirectoryState.Loading -> {
                            mutableState = mutableState.copy(
                                environmentsDirectoryPath = sideEffect.environmentsDirectoryPath,
                                environmentsDirectoryProcessResult = EnvironmentsDirectoryProcessResult.Loading,
                            )
                        }

                        is SetupEnvironmentHandler.SideEffect.UpdateEnvironmentsDirectoryState.Failure.FileIsNotDirectory -> {
                            mutableState = mutableState.copy(
                                environmentsDirectoryPath = sideEffect.environmentsDirectoryPath,
                                environmentsDirectoryProcessResult = EnvironmentsDirectoryProcessResult.Failure.FileIsNotDirectory,
                                environments = emptySet(),
                                schema = Schema(),
                            )
                        }

                        is SetupEnvironmentHandler.SideEffect.UpdateEnvironmentsDirectoryState.Failure.InvalidEnvironments -> {
                            mutableState = mutableState.copy(
                                environmentsDirectoryPath = sideEffect.environmentsDirectoryPath,
                                environmentsDirectoryProcessResult = EnvironmentsDirectoryProcessResult.Failure.InvalidEnvironments,
                                environments = emptySet(),
                                schema = Schema(),
                            )
                        }
                    }
                }
        }
    }

    private fun CreateEnvironmentAction.SetupSchemaAction.handle() {
        when (this) {
            is CreateEnvironmentAction.SetupSchemaAction.OnSupportedPlatformChanged -> {
                mutableState = mutableState.updateSchema {
                    updateSupportedPlatforms {
                        if (isChecked) this + newPlatformType else this - newPlatformType
                    }

                    updatePropertyDefinitions { schema ->
                        map { propertyDefinition ->
                            propertyDefinition.copy(
                                supportedPlatforms =
                                    propertyDefinition.supportedPlatforms intersect schema.supportedPlatforms,
                            )
                        }.toSet()
                    }
                }
            }

            is CreateEnvironmentAction.SetupSchemaAction.OnAddPropertyDefinitionClicked -> {
                mutableState = mutableState.updateSchema {
                    updatePropertyDefinitions { schema ->
                        this + schema.emptyPropertyDefinition()
                    }
                }
            }

            is CreateEnvironmentAction.SetupSchemaAction.OnPropertyNameChanged -> {
                mutableState = mutableState.updateSchema {
                    updatePropertyDefinitions {
                        updatePropertyDefinition(index) {
                            copy(name = newName)
                        }
                    }
                }
            }

            is CreateEnvironmentAction.SetupSchemaAction.OnPropertyTypeChanged -> {
                mutableState = mutableState.updateSchema {
                    updatePropertyDefinitions {
                        updatePropertyDefinition(index) {
                            copy(propertyType = newPropertyType)
                        }
                    }
                }
            }

            is CreateEnvironmentAction.SetupSchemaAction.OnNullableChanged -> {
                mutableState = mutableState.updateSchema {
                    updatePropertyDefinitions {
                        updatePropertyDefinition(index) {
                            copy(nullable = newValue)
                        }
                    }
                }
            }

            is CreateEnvironmentAction.SetupSchemaAction.OnPropertyDefinitionSupportedPlatformChanged -> {
                mutableState = mutableState.updateSchema {
                    updatePropertyDefinitions { schema ->
                        updatePropertyDefinition(index) {
                            copy(
                                supportedPlatforms = if (isChecked) {
                                    if (schema.supportedPlatforms.contains(newPlatformType)) {
                                        supportedPlatforms + newPlatformType
                                    } else {
                                        supportedPlatforms
                                    }
                                } else {
                                    supportedPlatforms - newPlatformType
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun CreateEnvironmentState.updateSchema(block: SchemaBuilder.() -> Unit): CreateEnvironmentState {
        val schemaBuilder = SchemaBuilder(schema)
        schemaBuilder.block()
        return copy(schema = schemaBuilder.build())
    }

    private fun Set<PropertyDefinition>.updatePropertyDefinition(
        index: Int,
        block: PropertyDefinition.() -> PropertyDefinition,
    ): Set<PropertyDefinition> =
        mapIndexed { currentIndex, currentPropertyDefinition ->
            if (currentIndex == index) currentPropertyDefinition.block() else currentPropertyDefinition
        }.toSet()

    private fun Schema.emptyPropertyDefinition(): PropertyDefinition = PropertyDefinition(
        name = "",
        propertyType = PropertyType.STRING,
        nullable = true,
        supportedPlatforms = supportedPlatforms,
    )

    private fun CreateEnvironmentAction.SetupEnvironmentAction.OnSelectEnvironmentPathClicked.handle() {
        val environmentsDirectoryPath = onEnvironmentsDirectorySelected() ?: return
        val environmentsDirectory = File(environmentsDirectoryPath)
        process(environmentsDirectory)
    }

    private fun CreateEnvironmentAction.SetupEnvironmentAction.OnEnvironmentNameChanged.handle() {
        mutableState = mutableState.copy(environmentName = newName)
    }

    private fun CreateEnvironmentAction.DialogAction.handle() {
        when (this) {
            is CreateEnvironmentAction.DialogAction.OnPreviousButtonClicked -> handle()
            is CreateEnvironmentAction.DialogAction.OnNextButtonClicked -> handle()
            is CreateEnvironmentAction.DialogAction.OnFinishButtonClicked -> Unit //TODO: Finish
        }
    }

    private fun CreateEnvironmentAction.DialogAction.OnPreviousButtonClicked.handle() {
        when (mutableState.step) {
            Step.SETUP_ENVIRONMENT -> Unit
            Step.SETUP_SCHEMA -> mutableState = mutableState.copy(step = Step.SETUP_ENVIRONMENT)
        }
    }

    private fun CreateEnvironmentAction.DialogAction.OnNextButtonClicked.handle() {
        when (mutableState.step) {
            Step.SETUP_ENVIRONMENT -> mutableState = mutableState.copy(step = Step.SETUP_SCHEMA)
            Step.SETUP_SCHEMA -> Unit
        }
    }

    private class SchemaBuilder(private var schema: Schema) {
        fun updateSupportedPlatforms(block: Set<PlatformType>.(schema: Schema) -> Set<PlatformType>) {
            schema = schema.copy(
                supportedPlatforms = schema.supportedPlatforms.block(schema)
            )
        }

        fun updatePropertyDefinitions(block: Set<PropertyDefinition>.(schema: Schema) -> Set<PropertyDefinition>) {
            schema = schema.copy(
                propertyDefinitions = schema.propertyDefinitions.block(schema)
            )
        }

        fun build(): Schema = schema
    }
}