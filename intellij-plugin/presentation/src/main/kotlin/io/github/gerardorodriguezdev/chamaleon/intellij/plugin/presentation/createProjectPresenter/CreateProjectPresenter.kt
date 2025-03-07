package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter

import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.models.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyType
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.asDelegate
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.handlers.SetupEnvironmentHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.CoroutineContext

internal class CreateProjectPresenter(
    private val uiScope: CoroutineScope,
    private val ioContext: CoroutineContext,

    private val projectDirectory: ExistingDirectory,

    private val setupEnvironmentHandler: SetupEnvironmentHandler,

    private val onEnvironmentsDirectorySelected: () -> ExistingDirectory?,
    private val onFinishButtonClicked: (state: CreateProjectState) -> Unit,
) {
    private val mutableStateFlow = MutableStateFlow<CreateProjectState>(CreateProjectState())
    val stateFlow: StateFlow<CreateProjectState> = mutableStateFlow

    private var mutableState by mutableStateFlow.asDelegate()

    private var processJob: Job? = null

    fun onAction(action: CreateProjectAction) {
        when (action) {
            is CreateProjectAction.SetupEnvironmentAction -> action.handle()
            is CreateProjectAction.SetupSchemaAction -> action.handle()
            is CreateProjectAction.SetupPropertiesAction -> action.handle()
            is CreateProjectAction.DialogAction -> action.handle()
        }
    }

    private fun CreateProjectAction.SetupEnvironmentAction.handle() =
        when (this) {
            is CreateProjectAction.SetupEnvironmentAction.OnInit -> process(projectDirectory)
            is CreateProjectAction.SetupEnvironmentAction.OnSelectEnvironmentPath -> handle()
            is CreateProjectAction.SetupEnvironmentAction.OnEnvironmentNameChanged -> handle()
        }

    private fun process(environmentsDirectory: File) {
        processJob?.cancel()

        processJob = uiScope
            .launch {
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
                                    globalSupportedPlatforms = sideEffect.schema.globalSupportedPlatformTypes,
                                    propertyDefinitions = sideEffect.schema.propertyDefinitions.toPropertyDefinitions(),
                                    allowUpdatingSchema =
                                        sideEffect.schema.globalSupportedPlatformTypes.isEmpty() &&
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
                supportedPlatforms = propertyDefinition.supportedPlatformTypes,
            )
        }.toSet()

    private fun CreateProjectAction.SetupSchemaAction.handle() {
        if (!mutableState.allowUpdatingSchema) return

        when (this) {
            is CreateProjectAction.SetupSchemaAction.OnSupportedPlatformChanged -> {
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

            is CreateProjectAction.SetupSchemaAction.OnAddPropertyDefinition -> {
                mutableState = mutableState
                    .updatePropertyDefinitions { globalSupportedPlatforms ->
                        this + emptyPropertyDefinition(globalSupportedPlatforms)
                    }
            }

            is CreateProjectAction.SetupSchemaAction.OnPropertyNameChanged -> {
                mutableState = mutableState
                    .updatePropertyDefinitions {
                        updatePropertyDefinition(index) {
                            copy(name = newName)
                        }
                    }
            }

            is CreateProjectAction.SetupSchemaAction.OnDeletePropertyDefinition -> {
                mutableState = mutableState
                    .updatePropertyDefinitions {
                        removeItemAt(index)
                    }
            }

            is CreateProjectAction.SetupSchemaAction.OnPropertyTypeChanged -> {
                mutableState = mutableState
                    .updatePropertyDefinitions {
                        updatePropertyDefinition(index) {
                            copy(propertyType = newPropertyType)
                        }
                    }
            }

            is CreateProjectAction.SetupSchemaAction.OnNullableChanged -> {
                mutableState = mutableState
                    .updatePropertyDefinitions {
                        updatePropertyDefinition(index) {
                            copy(nullable = newValue)
                        }
                    }
            }

            is CreateProjectAction.SetupSchemaAction.OnPropertyDefinitionSupportedPlatformChanged -> {
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

    private fun CreateProjectState.updateGlobalSupportedPlatforms(
        block: Set<PlatformType>.() -> Set<PlatformType>
    ): CreateProjectState =
        copy(globalSupportedPlatforms = globalSupportedPlatforms.block())

    private fun CreateProjectState.updatePropertyDefinitions(
        block: Set<PropertyDefinition>.(globalSupportedPlatforms: Set<PlatformType>) -> Set<PropertyDefinition>
    ): CreateProjectState =
        copy(propertyDefinitions = propertyDefinitions.block(globalSupportedPlatforms))

    private fun Set<PropertyDefinition>.updatePropertyDefinition(
        index: Int,
        block: PropertyDefinition.() -> PropertyDefinition,
    ): Set<PropertyDefinition> =
        mapIndexed { currentIndex, currentPropertyDefinition ->
            if (currentIndex == index) currentPropertyDefinition.block() else currentPropertyDefinition
        }.toSet()

    private fun CreateProjectState.updatePlatform(
        platformType: PlatformType,
        block: Platform.() -> Platform,
    ): CreateProjectState =
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

    private fun CreateProjectAction.SetupEnvironmentAction.OnSelectEnvironmentPath.handle() {
        val environmentsDirectoryPath = onEnvironmentsDirectorySelected() ?: return
        val environmentsDirectory = File(environmentsDirectoryPath)
        process(environmentsDirectory)
    }

    private fun CreateProjectAction.SetupEnvironmentAction.OnEnvironmentNameChanged.handle() {
        mutableState = mutableState.copy(environmentName = newName)
    }

    private fun CreateProjectAction.SetupPropertiesAction.handle() =
        when (this) {
            is CreateProjectAction.SetupPropertiesAction.OnPropertyValueChanged -> {
                mutableState = mutableState
                    .updatePlatform(platformType) {
                        updatePropertyValue(
                            index = index,
                            propertyValue = newValue,
                        )
                    }
            }
        }

    private fun CreateProjectAction.DialogAction.handle() {
        when (this) {
            is CreateProjectAction.DialogAction.OnPreviousButtonClicked -> handle()
            is CreateProjectAction.DialogAction.OnNextButtonClicked -> handle()
            is CreateProjectAction.DialogAction.OnFinishButtonClicked -> onFinishButtonClicked(mutableState)
        }
    }

    private fun CreateProjectAction.DialogAction.OnPreviousButtonClicked.handle() {
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

    private fun CreateProjectAction.DialogAction.OnNextButtonClicked.handle() {
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

private fun CreateProjectState.initialPlatforms(): Set<Platform> =
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

private fun CreateProjectState.propertiesForPlatform(platformType: PlatformType): List<PropertyDefinition> =
    propertyDefinitions.filter { propertyDefinition ->
        propertyDefinition.supportedPlatforms.contains(platformType)
    }

private fun Set<PropertyDefinition>.removeItemAt(index: Int): Set<PropertyDefinition> =
    mapIndexedNotNull { currentIndex, item ->
        if (index == currentIndex) null else item
    }.toSet()