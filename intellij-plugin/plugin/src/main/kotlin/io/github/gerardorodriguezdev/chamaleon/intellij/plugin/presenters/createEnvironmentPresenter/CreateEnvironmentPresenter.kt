package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter

import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyType
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.asDelegate
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState.Platform.Property
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState.Platform.Property.PropertyValue
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
    private val onFinishButtonClicked: (state: CreateEnvironmentState) -> Unit,
) {
    private val mutableStateFlow = MutableStateFlow<CreateEnvironmentState>(CreateEnvironmentState())
    val stateFlow: StateFlow<CreateEnvironmentState> = mutableStateFlow

    private var mutableState by mutableStateFlow.asDelegate()

    private var processJob: Job? = null

    fun onAction(action: CreateEnvironmentAction) {
        when (action) {
            is CreateEnvironmentAction.SetupEnvironmentAction -> action.handle()
            is CreateEnvironmentAction.SetupSchemaAction -> action.handle()
            is CreateEnvironmentAction.SetupPropertiesAction -> action.handle()
            is CreateEnvironmentAction.DialogAction -> action.handle()
        }
    }

    private fun CreateEnvironmentAction.SetupEnvironmentAction.handle() =
        when (this) {
            is CreateEnvironmentAction.SetupEnvironmentAction.OnInit -> process(projectDirectory)
            is CreateEnvironmentAction.SetupEnvironmentAction.OnSelectEnvironmentPath -> handle()
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
                                environmentsNames = sideEffect.environments.environmentNames(),
                                globalSupportedPlatforms = sideEffect.schema.globalSupportedPlatforms,
                                propertyDefinitions = sideEffect.schema.propertyDefinitions.toPropertyDefinitions(),
                                allowUpdatingSchema =
                                    sideEffect.schema.globalSupportedPlatforms.isEmpty() &&
                                            sideEffect.schema.propertyDefinitions.isEmpty()
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
                                environmentsNames = emptySet(),
                                globalSupportedPlatforms = emptySet(),
                                propertyDefinitions = emptySet(),
                            )
                        }

                        is SetupEnvironmentHandler.SideEffect.UpdateEnvironmentsDirectoryState.Failure.InvalidEnvironments -> {
                            mutableState = mutableState.copy(
                                environmentsDirectoryPath = sideEffect.environmentsDirectoryPath,
                                environmentsDirectoryProcessResult = EnvironmentsDirectoryProcessResult.Failure.InvalidEnvironments,
                                environmentsNames = emptySet(),
                                globalSupportedPlatforms = emptySet(),
                                propertyDefinitions = emptySet(),
                            )
                        }
                    }
                }
        }
    }

    private fun Set<Environment>.environmentNames(): Set<String> =
        map { environment -> environment.name }.toSet()

    private fun Set<Schema.PropertyDefinition>.toPropertyDefinitions(): Set<PropertyDefinition> =
        map { propertyDefinition ->
            PropertyDefinition(
                name = propertyDefinition.name,
                propertyType = propertyDefinition.propertyType,
                nullable = propertyDefinition.nullable,
                supportedPlatforms = propertyDefinition.supportedPlatforms,
            )
        }.toSet()

    private fun CreateEnvironmentAction.SetupSchemaAction.handle() {
        if (!mutableState.allowUpdatingSchema) return

        when (this) {
            is CreateEnvironmentAction.SetupSchemaAction.OnSupportedPlatformChanged -> {
                mutableState = mutableState
                    .updateGlobalSupportedPlatforms {
                        if (isChecked) this + newPlatformType else this - newPlatformType
                    }
                    .updatePropertyDefinitions { globalSupportedPlatforms ->
                        map { propertyDefinition ->
                            propertyDefinition.copy(
                                supportedPlatforms =
                                    propertyDefinition.supportedPlatforms intersect globalSupportedPlatforms,
                            )
                        }.toSet()
                    }
            }

            is CreateEnvironmentAction.SetupSchemaAction.OnAddPropertyDefinition -> {
                mutableState = mutableState
                    .updatePropertyDefinitions { globalSupportedPlatforms ->
                        this + emptyPropertyDefinition(globalSupportedPlatforms)
                    }
            }

            is CreateEnvironmentAction.SetupSchemaAction.OnPropertyNameChanged -> {
                mutableState = mutableState
                    .updatePropertyDefinitions {
                        updatePropertyDefinition(index) {
                            copy(name = newName)
                        }
                    }
            }

            is CreateEnvironmentAction.SetupSchemaAction.OnDeletePropertyDefinition -> {
                mutableState = mutableState
                    .updatePropertyDefinitions {
                        removeItemAt(index)
                    }
            }

            is CreateEnvironmentAction.SetupSchemaAction.OnPropertyTypeChanged -> {
                mutableState = mutableState
                    .updatePropertyDefinitions {
                        updatePropertyDefinition(index) {
                            copy(propertyType = newPropertyType)
                        }
                    }
            }

            is CreateEnvironmentAction.SetupSchemaAction.OnNullableChanged -> {
                mutableState = mutableState
                    .updatePropertyDefinitions {
                        updatePropertyDefinition(index) {
                            copy(nullable = newValue)
                        }
                    }
            }

            is CreateEnvironmentAction.SetupSchemaAction.OnPropertyDefinitionSupportedPlatformChanged -> {
                mutableState = mutableState
                    .updatePropertyDefinitions { globalSupportedPlatforms ->
                        updatePropertyDefinition(index) {
                            copy(
                                supportedPlatforms = if (isChecked) {
                                    if (globalSupportedPlatforms.contains(newPlatformType)) {
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

    private fun CreateEnvironmentState.updateGlobalSupportedPlatforms(
        block: Set<PlatformType>.() -> Set<PlatformType>
    ): CreateEnvironmentState =
        copy(globalSupportedPlatforms = globalSupportedPlatforms.block())

    private fun CreateEnvironmentState.updatePropertyDefinitions(
        block: Set<PropertyDefinition>.(globalSupportedPlatforms: Set<PlatformType>) -> Set<PropertyDefinition>
    ): CreateEnvironmentState =
        copy(propertyDefinitions = propertyDefinitions.block(globalSupportedPlatforms))

    private fun Set<PropertyDefinition>.updatePropertyDefinition(
        index: Int,
        block: PropertyDefinition.() -> PropertyDefinition,
    ): Set<PropertyDefinition> =
        mapIndexed { currentIndex, currentPropertyDefinition ->
            if (currentIndex == index) currentPropertyDefinition.block() else currentPropertyDefinition
        }.toSet()

    private fun CreateEnvironmentState.updatePlatform(
        platformType: PlatformType,
        block: Platform.() -> Platform,
    ): CreateEnvironmentState =
        copy(
            platforms = platforms.map { platform ->
                if (platform.platformType == platformType) {
                    platform.block()
                } else {
                    platform
                }
            }.toSet()
        )

    private fun Platform.updatePropertyValue(
        index: Int,
        propertyValue: PropertyValue
    ): Platform =
        copy(
            properties = properties.mapIndexed { currentIndex, property ->
                if (index == currentIndex) {
                    property.copy(value = propertyValue)
                } else {
                    property
                }
            }.toSet()
        )

    private fun emptyPropertyDefinition(globalSupportedPlatforms: Set<PlatformType>): PropertyDefinition =
        PropertyDefinition(
            name = "",
            propertyType = PropertyType.STRING,
            nullable = true,
            supportedPlatforms = globalSupportedPlatforms,
        )

    private fun CreateEnvironmentAction.SetupEnvironmentAction.OnSelectEnvironmentPath.handle() {
        val environmentsDirectoryPath = onEnvironmentsDirectorySelected() ?: return
        val environmentsDirectory = File(environmentsDirectoryPath)
        process(environmentsDirectory)
    }

    private fun CreateEnvironmentAction.SetupEnvironmentAction.OnEnvironmentNameChanged.handle() {
        mutableState = mutableState.copy(environmentName = newName)
    }

    private fun CreateEnvironmentAction.SetupPropertiesAction.handle() =
        when (this) {
            is CreateEnvironmentAction.SetupPropertiesAction.OnPropertyValueChanged -> {
                mutableState = mutableState
                    .updatePlatform(platformType) {
                        updatePropertyValue(
                            index = index,
                            propertyValue = newValue,
                        )
                    }
            }
        }

    private fun CreateEnvironmentAction.DialogAction.handle() {
        when (this) {
            is CreateEnvironmentAction.DialogAction.OnPreviousButtonClicked -> handle()
            is CreateEnvironmentAction.DialogAction.OnNextButtonClicked -> handle()
            is CreateEnvironmentAction.DialogAction.OnFinishButtonClicked -> onFinishButtonClicked(mutableState)
        }
    }

    private fun CreateEnvironmentAction.DialogAction.OnPreviousButtonClicked.handle() {
        when (mutableState.step) {
            Step.SETUP_ENVIRONMENT -> Unit
            Step.SETUP_SCHEMA ->
                mutableState = mutableState.copy(
                    step = Step.SETUP_ENVIRONMENT,
                    globalSupportedPlatforms = if (mutableState.allowUpdatingSchema) {
                        emptySet()
                    } else {
                        mutableState.globalSupportedPlatforms
                    },
                    propertyDefinitions = if (mutableState.allowUpdatingSchema) {
                        emptySet()
                    } else {
                        mutableState.propertyDefinitions
                    },
                )

            Step.SETUP_PROPERTIES ->
                mutableState = mutableState.copy(
                    step = Step.SETUP_SCHEMA,
                    platforms = emptySet(),
                )
        }
    }

    private fun CreateEnvironmentAction.DialogAction.OnNextButtonClicked.handle() {
        when (mutableState.step) {
            Step.SETUP_ENVIRONMENT -> mutableState = mutableState.copy(step = Step.SETUP_SCHEMA)
            Step.SETUP_SCHEMA -> {
                mutableState = mutableState.copy(
                    step = Step.SETUP_PROPERTIES,
                    platforms = mutableState.initialPlatforms(),
                )
            }

            Step.SETUP_PROPERTIES -> Unit
        }
    }
}

private fun CreateEnvironmentState.initialPlatforms(): Set<Platform> =
    globalSupportedPlatforms.map { platformType ->
        val propertiesForPlatform = propertiesForPlatform(platformType)

        Platform(
            platformType = platformType,
            properties = propertiesForPlatform.toProperties()
        )
    }.toSet()


private fun List<PropertyDefinition>.toProperties(): Set<Property> =
    map { propertyDefinition ->
        Property(
            name = propertyDefinition.name,
            value = propertyDefinition.propertyType.initialPropertyValue(propertyDefinition.nullable),
        )
    }.toSet()

private fun PropertyType.initialPropertyValue(nullable: Boolean): PropertyValue =
    when (this) {
        PropertyType.STRING -> PropertyValue.StringProperty("")
        PropertyType.BOOLEAN -> if (nullable) {
            PropertyValue.NullableBooleanProperty(false)
        } else {
            PropertyValue.BooleanProperty(false)
        }
    }

private fun CreateEnvironmentState.propertiesForPlatform(platformType: PlatformType): List<PropertyDefinition> =
    propertyDefinitions.filter { propertyDefinition ->
        propertyDefinition.supportedPlatforms.contains(platformType)
    }

private fun Set<PropertyDefinition>.removeItemAt(index: Int): Set<PropertyDefinition> =
    mapIndexedNotNull { currentIndex, item ->
        if (index == currentIndex) null else item
    }.toSet()