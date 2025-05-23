@file:Suppress("TooManyFunctions")

package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.createProjectDialog.mappers

import io.github.gerardorodriguezdev.chamaleon.core.models.*
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema.PropertyDefinition
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptySet
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.CreateProjectState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.CreateProjectState.SetupEnvironment.ProjectDeserializationState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsProvider
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.models.Field
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.models.Field.Verification
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList

internal fun CreateProjectState.toCreateProjectWindowState(
    projectDirectoryPath: String,
    stringsProvider: StringsProvider
): CreateProjectWindowState? {
    val context = Context(projectDirectoryPath, stringsProvider)

    return when (this) {
        is CreateProjectState.SetupEnvironment -> context.toSetupEnvironment(this)
        is CreateProjectState.SetupSchema -> context.toSetupSchema(this)
        is CreateProjectState.SetupPlatforms -> context.toSetupPlatforms(this)
    }
}

private fun Context.toSetupEnvironment(
    state: CreateProjectState.SetupEnvironment
): CreateProjectWindowState.SetupEnvironmentState {
    val projectDeserializationState = state.projectDeserializationState
    return CreateProjectWindowState.SetupEnvironmentState(
        environmentsDirectoryPathField = toEnvironmentsDirectoryPathField(projectDeserializationState),
        environmentNameField = toEnvironmentNameField(state.environmentName, projectDeserializationState),
    )
}

private fun Context.toEnvironmentsDirectoryPathField(
    projectDeserializationState: ProjectDeserializationState?
): Field<String> =
    when (projectDeserializationState) {
        null -> Field(value = "", verification = null)

        is ProjectDeserializationState.Valid -> Field(
            value = environmentsDirectoryPathValue(projectDeserializationState),
            verification = null,
        )

        is ProjectDeserializationState.Loading ->
            Field(
                value = environmentsDirectoryPathValue(projectDeserializationState),
                verification = Verification.Loading,
            )

        is ProjectDeserializationState.Invalid ->
            Field(
                value = environmentsDirectoryPathValue(projectDeserializationState),
                verification = Verification.Invalid(projectDeserializationState.errorMessage),
            )
    }

private fun Context.environmentsDirectoryPathValue(projectDeserializationState: ProjectDeserializationState): String =
    projectDeserializationState.environmentsDirectoryPath.value.removePrefix(projectDirectoryPath)

private fun Context.toEnvironmentNameField(
    environmentName: NonEmptyString?,
    projectDeserializationState: ProjectDeserializationState?,
): Field<String> {
    return if (environmentName == null) {
        Field(
            value = "",
            verification = Verification.Invalid(stringsProvider.string(StringsKeys.environmentNameEmpty))
        )
    } else {
        when (projectDeserializationState) {
            is ProjectDeserializationState.Valid -> toEnvironmentNameField(environmentName, projectDeserializationState)
            else -> Field(value = environmentName.value, verification = null)
        }
    }
}

@Suppress("MaxLineLength")
private fun Context.toEnvironmentNameField(
    environmentName: NonEmptyString,
    projectDeserializationState: ProjectDeserializationState.Valid,
): Field<String> =
    when (projectDeserializationState) {
        is ProjectDeserializationState.Valid.NewProject ->
            Field(value = environmentName.value, verification = null)

        is ProjectDeserializationState.Valid.ExistingProject ->
            Field(
                value = environmentName.value,
                verification = if (projectDeserializationState.currentProject.environments?.contains(key = environmentName.value) == true) {
                    Verification.Invalid(stringsProvider.string(StringsKeys.environmentNameIsDuplicated))
                } else {
                    null
                }
            )
    }

private fun Context.toSetupSchema(state: CreateProjectState.SetupSchema): CreateProjectWindowState.SetupSchemaState =
    when (state) {
        is CreateProjectState.SetupSchema.NewSchema -> CreateProjectWindowState.SetupSchemaState(
            title = stringsProvider.string(StringsKeys.createTemplate),
            globalSupportedPlatformTypes = state.globalSupportedPlatformTypes?.toSupportedPlatformTypes()
                ?: persistentListOf(),
            propertyDefinitions = toPropertyDefinitions(state.propertyDefinitions),
        )

        is CreateProjectState.SetupSchema.ExistingSchema ->
            CreateProjectWindowState.SetupSchemaState(
                title = stringsProvider.string(StringsKeys.selectedTemplate),
                globalSupportedPlatformTypes =
                state.currentProject.schema.globalSupportedPlatformTypes.toSupportedPlatformTypes(),
                propertyDefinitions = toPropertyDefinitions(state.currentProject.schema),
            )
    }

private fun Context.toPropertyDefinitions(
    propertyDefinitions: List<CreateProjectState.SetupSchema.NewSchema.PropertyDefinition>,
): ImmutableList<CreateProjectWindowState.SetupSchemaState.PropertyDefinition> =
    propertyDefinitions.map { propertyDefinition ->
        val isDuplicated = propertyDefinition.isDuplicated(propertyDefinitions)
        toPropertyDefinition(isDuplicated = isDuplicated, propertyDefinition = propertyDefinition)
    }.toPersistentList()

private fun CreateProjectState.SetupSchema.NewSchema.PropertyDefinition.isDuplicated(
    propertyDefinitions: List<CreateProjectState.SetupSchema.NewSchema.PropertyDefinition>
): Boolean =
    propertyDefinitions.count { currentPropertyDefinition ->
        currentPropertyDefinition.name == name
    } > 1

private fun Context.toPropertyDefinitions(
    schema: Schema
): ImmutableList<CreateProjectWindowState.SetupSchemaState.PropertyDefinition> =
    schema.propertyDefinitions.values.map { propertyDefinition ->
        propertyDefinition.toPropertyDefinition(schema.globalSupportedPlatformTypes)
    }.toImmutableList()

private fun Context.toPropertyDefinition(
    isDuplicated: Boolean,
    propertyDefinition: CreateProjectState.SetupSchema.NewSchema.PropertyDefinition,
): CreateProjectWindowState.SetupSchemaState.PropertyDefinition =
    CreateProjectWindowState.SetupSchemaState.PropertyDefinition(
        nameField = Field(
            value = propertyDefinition.name?.value ?: "",
            verification = when {
                propertyDefinition.name == null ->
                    Verification.Invalid(stringsProvider.string(StringsKeys.emptyPropertyDefinitionName))

                isDuplicated ->
                    Verification.Invalid(stringsProvider.string(StringsKeys.propertyDefinitionIsDuplicated))

                else -> null
            }
        ),
        propertyType = propertyDefinition.propertyType.toPropertyType(),
        nullable = propertyDefinition.nullable,
        supportedPlatformTypes = propertyDefinition.supportedPlatformTypes?.toSupportedPlatformTypes()
            ?: persistentListOf(),
    )

@Suppress("Indentation")
private fun PropertyDefinition.toPropertyDefinition(
    globalSupportedPlatformTypes: NonEmptySet<PlatformType>
): CreateProjectWindowState.SetupSchemaState.PropertyDefinition =
    CreateProjectWindowState.SetupSchemaState.PropertyDefinition(
        nameField = Field(
            value = name.value,
            verification = null,
        ),
        propertyType = propertyType.toPropertyType(),
        nullable = nullable,
        supportedPlatformTypes =
        supportedPlatformTypes?.toSupportedPlatformTypes()
            ?: globalSupportedPlatformTypes.toSupportedPlatformTypes(),
    )

private fun PropertyType.toPropertyType(): CreateProjectWindowState.SetupSchemaState.PropertyDefinition.PropertyType =
    when (this) {
        PropertyType.STRING -> CreateProjectWindowState.SetupSchemaState.PropertyDefinition.PropertyType.STRING
        PropertyType.BOOLEAN -> CreateProjectWindowState.SetupSchemaState.PropertyDefinition.PropertyType.BOOLEAN
    }

@Suppress("MaxLineLength")
private fun NonEmptySet<PlatformType>.toSupportedPlatformTypes(): ImmutableList<CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType> =
    map { platformType -> platformType.toPlatformType() }.toPersistentList()

private fun PlatformType.toPlatformType(): CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType =
    when (this) {
        PlatformType.ANDROID -> CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType.ANDROID
        PlatformType.WASM -> CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType.WASM
        PlatformType.NATIVE -> CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType.NATIVE
        PlatformType.JS -> CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType.JS
        PlatformType.JVM -> CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType.JVM
    }

private fun Context.toSetupPlatforms(
    state: CreateProjectState.SetupPlatforms
): CreateProjectWindowState.SetupPlatformsState =
    when (state) {
        is CreateProjectState.SetupPlatforms.NewProject ->
            CreateProjectWindowState.SetupPlatformsState(
                platforms = toPlatforms(state.platforms, state.schema)
            )

        is CreateProjectState.SetupPlatforms.ExistingProject ->
            CreateProjectWindowState.SetupPlatformsState(
                platforms = toPlatforms(state.platforms, state.currentProject.schema)
            )
    }

private fun Context.toPlatforms(
    platforms: NonEmptyKeySetStore<PlatformType, Platform>,
    schema: Schema,
): ImmutableList<CreateProjectWindowState.SetupPlatformsState.Platform> =
    platforms.values.map { platform ->
        CreateProjectWindowState.SetupPlatformsState.Platform(
            platformType = platform.platformType.toPlatformType(),
            properties = toProperties(platform.properties, schema.propertyDefinitions)
        )
    }.toPersistentList()

private fun Context.toProperties(
    properties: NonEmptyKeySetStore<String, Platform.Property>,
    propertyDefinitions: NonEmptyKeySetStore<String, PropertyDefinition>,
): ImmutableList<CreateProjectWindowState.SetupPlatformsState.Platform.Property> =
    properties.values.mapNotNull { property ->
        val propertyDefinition = propertyDefinitions[property.name.value] ?: return@mapNotNull null
        CreateProjectWindowState.SetupPlatformsState.Platform.Property(
            name = property.name.value,
            value = toPropertyValue(property.value, propertyDefinition),
        )
    }.toImmutableList()

private fun Context.toPropertyValue(
    propertyValue: PropertyValue?,
    propertyDefinition: PropertyDefinition,
): CreateProjectWindowState.SetupPlatformsState.Platform.Property.PropertyValue =
    when (propertyValue) {
        null ->
            toPropertyValue(
                propertyType = propertyDefinition.propertyType,
                nullable = propertyDefinition.nullable,
            )

        is PropertyValue.StringProperty ->
            CreateProjectWindowState.SetupPlatformsState.Platform.Property.PropertyValue.StringProperty(
                Field(
                    value = propertyValue.value.value,
                    verification = null,
                )
            )

        is PropertyValue.BooleanProperty -> if (propertyDefinition.nullable) {
            CreateProjectWindowState.SetupPlatformsState.Platform.Property.PropertyValue.NullableBooleanProperty(
                value = propertyValue.value
            )
        } else {
            CreateProjectWindowState.SetupPlatformsState.Platform.Property.PropertyValue.BooleanProperty(
                value = propertyValue.value
            )
        }
    }

private fun Context.toPropertyValue(
    propertyType: PropertyType,
    nullable: Boolean,
): CreateProjectWindowState.SetupPlatformsState.Platform.Property.PropertyValue =
    when (propertyType) {
        PropertyType.STRING ->
            CreateProjectWindowState.SetupPlatformsState.Platform.Property.PropertyValue.StringProperty(
                Field(
                    value = "",
                    verification = if (nullable) {
                        null
                    } else {
                        Verification.Invalid(stringsProvider.string(StringsKeys.valueEmptyButNotNullable))
                    }
                )
            )

        PropertyType.BOOLEAN ->
            CreateProjectWindowState.SetupPlatformsState.Platform.Property.PropertyValue.NullableBooleanProperty(null)
    }

private data class Context(
    val projectDirectoryPath: String,
    val stringsProvider: StringsProvider,
)