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
        is CreateProjectState.Finish -> null
    }

}

private fun Context.toSetupEnvironment(state: CreateProjectState.SetupEnvironment): CreateProjectWindowState.SetupEnvironmentState {
    val projectDeserializationState = state.projectDeserializationState
    return CreateProjectWindowState.SetupEnvironmentState(
        environmentsDirectoryPathField = toEnvironmentsDirectoryPathField(projectDeserializationState),
        environmentNameField = toEnvironmentNameField(state.environmentName, projectDeserializationState),
    )
}

private fun Context.toEnvironmentsDirectoryPathField(projectDeserializationState: ProjectDeserializationState?): Field<String> {
    return when (projectDeserializationState) {
        null -> Field(value = "", verification = null)

        is ProjectDeserializationState.Valid ->
            Field(value = "", verification = Verification.Valid)

        is ProjectDeserializationState.Loading ->
            Field(
                value = projectDeserializationState.environmentsDirectory.path.value,
                verification = Verification.Loading
            )

        is ProjectDeserializationState.Invalid ->
            Field(
                value = projectDeserializationState.environmentsDirectory.path.value,
                verification = Verification.Invalid(projectDeserializationState.errorMessage),
            )
    }
}

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
            is ProjectDeserializationState.Valid.NewProject ->
                Field(value = environmentName.value, verification = Verification.Valid)

            is ProjectDeserializationState.Valid.ExistingProject ->
                Field(
                    value = environmentName.value,
                    verification = if (projectDeserializationState.currentProject.environments?.contains(key = environmentName.value) == true) {
                        Verification.Invalid(stringsProvider.string(StringsKeys.environmentNameIsDuplicated))
                    } else {
                        Verification.Valid
                    }
                )

            else -> Field(value = environmentName.value, verification = null)
        }
    }
}

private fun Context.toSetupSchema(state: CreateProjectState.SetupSchema): CreateProjectWindowState.SetupSchemaState =
    when (state) {
        is CreateProjectState.SetupSchema.NewSchema -> CreateProjectWindowState.SetupSchemaState(
            title = stringsProvider.string(StringsKeys.createTemplate),
            globalSupportedPlatformTypes = state.globalSupportedPlatformTypes.toSupportedPlatformTypes(),
            propertyDefinitions = toPropertyDefinitions(state.propertyDefinitions),
        )

        is CreateProjectState.SetupSchema.ExistingSchema ->
            CreateProjectWindowState.SetupSchemaState(
                title = stringsProvider.string(StringsKeys.selectedTemplate),
                globalSupportedPlatformTypes = state.currentProject.schema.globalSupportedPlatformTypes.toSupportedPlatformTypes(),
                propertyDefinitions = state.currentProject.schema.propertyDefinitions.toPropertyDefinitions(),
            )
    }

private fun Context.toPropertyDefinitions(
    propertyDefinitions: List<CreateProjectState.SetupSchema.NewSchema.PropertyDefinition>,
): ImmutableList<CreateProjectWindowState.SetupSchemaState.PropertyDefinition> =
    propertyDefinitions.map { propertyDefinition ->
        toPropertyDefinition(propertyDefinition)
    }.toPersistentList()

private fun Context.toPropertyDefinition(
    propertyDefinition: CreateProjectState.SetupSchema.NewSchema.PropertyDefinition,
): CreateProjectWindowState.SetupSchemaState.PropertyDefinition =
    CreateProjectWindowState.SetupSchemaState.PropertyDefinition(
        nameField = Field(
            value = propertyDefinition.name?.value ?: "",
            verification = if (propertyDefinition.name == null) {
                Verification.Invalid(stringsProvider.string(StringsKeys.emptyPropertyDefinitionName))
            } else {
                Verification.Valid
            }
        ),
        propertyType = propertyDefinition.propertyType.toPropertyType(),
        nullable = propertyDefinition.nullable,
        supportedPlatformTypes = propertyDefinition.supportedPlatformTypes.toSupportedPlatformTypes(),
    )

private fun NonEmptyKeySetStore<String, Schema.PropertyDefinition>?.toPropertyDefinitions(): ImmutableList<CreateProjectWindowState.SetupSchemaState.PropertyDefinition> =
    this?.values?.map { propertyDefinition ->
        propertyDefinition.toPropertyDefinition()
    }?.toImmutableList() ?: persistentListOf()

private fun PropertyDefinition.toPropertyDefinition(): CreateProjectWindowState.SetupSchemaState.PropertyDefinition =
    CreateProjectWindowState.SetupSchemaState.PropertyDefinition(
        nameField = Field(
            value = name.value,
            verification = Verification.Valid,
        ),
        propertyType = propertyType.toPropertyType(),
        nullable = nullable,
        supportedPlatformTypes = supportedPlatformTypes.toSupportedPlatformTypes(),
    )

private fun PropertyType.toPropertyType(): CreateProjectWindowState.SetupSchemaState.PropertyDefinition.PropertyType =
    when (this) {
        PropertyType.STRING -> CreateProjectWindowState.SetupSchemaState.PropertyDefinition.PropertyType.STRING
        PropertyType.BOOLEAN -> CreateProjectWindowState.SetupSchemaState.PropertyDefinition.PropertyType.BOOLEAN
    }

private fun NonEmptySet<PlatformType>?.toSupportedPlatformTypes(): ImmutableList<CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType> =
    this?.map { platformType -> platformType.toPlatformType() }?.toPersistentList() ?: persistentListOf()

private fun PlatformType.toPlatformType(): CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType =
    when (this) {
        PlatformType.ANDROID -> CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType.ANDROID
        PlatformType.WASM -> CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType.WASM
        PlatformType.NATIVE -> CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType.NATIVE
        PlatformType.JS -> CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType.JS
        PlatformType.JVM -> CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType.JVM
    }

private fun Context.toSetupPlatforms(state: CreateProjectState.SetupPlatforms): CreateProjectWindowState.SetupPlatformsState =
    when (state) {
        is CreateProjectState.SetupPlatforms.NewProject ->
            CreateProjectWindowState.SetupPlatformsState(
                platforms = state.platforms.values.map { platform ->
                    CreateProjectWindowState.SetupPlatformsState.Platform(
                        platformType = platform.platformType.toPlatformType(),
                        properties = toProperties(platform.properties, state.schema.propertyDefinitions)
                    )
                }.toPersistentList(),
            )

        is CreateProjectState.SetupPlatforms.ExistingProject ->
            CreateProjectWindowState.SetupPlatformsState(
                platforms = state.platforms.values.map { platform ->
                    CreateProjectWindowState.SetupPlatformsState.Platform(
                        platformType = platform.platformType.toPlatformType(),
                        properties = toProperties(platform.properties, state.currentProject.schema.propertyDefinitions)
                    )
                }.toPersistentList()
            )
    }

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
        null -> toPropertyValue(propertyDefinition.propertyType)

        is PropertyValue.StringProperty ->
            CreateProjectWindowState.SetupPlatformsState.Platform.Property.PropertyValue.StringProperty(
                Field(
                    value = propertyValue.value.value,
                    verification = null,
                )
            )

        is PropertyValue.BooleanProperty ->
            CreateProjectWindowState.SetupPlatformsState.Platform.Property.PropertyValue.BooleanProperty(
                propertyValue.value
            )
    }

private fun Context.toPropertyValue(
    propertyType: PropertyType,
): CreateProjectWindowState.SetupPlatformsState.Platform.Property.PropertyValue =
    when (propertyType) {
        PropertyType.STRING ->
            CreateProjectWindowState.SetupPlatformsState.Platform.Property.PropertyValue.StringProperty(
                Field(
                    value = "",
                    verification = Verification.Invalid(stringsProvider.string(StringsKeys.valueEmptyButNotNullable))
                )
            )

        PropertyType.BOOLEAN ->
            CreateProjectWindowState.SetupPlatformsState.Platform.Property.PropertyValue.NullableBooleanProperty(null)
    }

private data class Context(
    val projectDirectoryPath: String,
    val stringsProvider: StringsProvider,
)