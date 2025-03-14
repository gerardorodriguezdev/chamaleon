package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.createProjectDialog.mappers

import io.github.gerardorodriguezdev.chamaleon.core.models.Platform
import io.github.gerardorodriguezdev.chamaleon.core.models.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyType
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema
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

//TODO: Fix
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
        if (projectDeserializationState is ProjectDeserializationState.Valid) {
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
            }
        } else {
            Field(value = environmentName.value, verification = null)
        }
    }
}

private fun Context.toSetupSchema(state: CreateProjectState.SetupSchema): CreateProjectWindowState.SetupSchemaState =
    when (state) {
        is CreateProjectState.SetupSchema.NewSchema -> CreateProjectWindowState.SetupSchemaState(
            title = stringsProvider.string(StringsKeys.createTemplate),
            globalSupportedPlatformTypes = state.globalSupportedPlatformTypes.toSupportedPlatformTypes(),
            propertyDefinitions = state.propertyDefinitions.toPropertyDefinitions(stringsProvider),
        )

        is CreateProjectState.SetupSchema.ExistingSchema ->
            CreateProjectWindowState.SetupSchemaState(
                title = stringsProvider.string(StringsKeys.selectedTemplate),
                globalSupportedPlatformTypes = state.currentProject.schema.globalSupportedPlatformTypes.toSupportedPlatformTypes(),
                propertyDefinitions = state.currentProject.schema.propertyDefinitions.toPropertyDefinitions(),
            )
    }

private fun List<CreateProjectState.SetupSchema.NewSchema.PropertyDefinition>.toPropertyDefinitions(
    stringsProvider: StringsProvider
): ImmutableList<CreateProjectWindowState.SetupSchemaState.PropertyDefinition> =
    map { propertyDefinition -> propertyDefinition.toPropertyDefinition(stringsProvider) }.toPersistentList()

private fun CreateProjectState.SetupSchema.NewSchema.PropertyDefinition.toPropertyDefinition(
    stringsProvider: StringsProvider
): CreateProjectWindowState.SetupSchemaState.PropertyDefinition =
    CreateProjectWindowState.SetupSchemaState.PropertyDefinition(
        nameField = Field(
            value = name?.value ?: "",
            verification = if (name == null) {
                Verification.Invalid(stringsProvider.string(StringsKeys.emptyPropertyDefinitionName))
            } else {
                Verification.Valid
            }
        ),
        propertyType = propertyType.toPropertyType(),
        nullable = nullable,
        supportedPlatformTypes = supportedPlatformTypes.toSupportedPlatformTypes(),
    )

private fun NonEmptyKeySetStore<String, Schema.PropertyDefinition>?.toPropertyDefinitions(): ImmutableList<CreateProjectWindowState.SetupSchemaState.PropertyDefinition> {
    if (this == null) return persistentListOf()

    return values.map { propertyDefinition -> propertyDefinition.toPropertyDefinition() }.toImmutableList()
}

private fun Schema.PropertyDefinition.toPropertyDefinition(): CreateProjectWindowState.SetupSchemaState.PropertyDefinition =
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

private fun NonEmptySet<PlatformType>?.toSupportedPlatformTypes(): ImmutableList<CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType> {
    if (this == null) return persistentListOf()

    return map { platformType ->
        platformType.toPlatformType()
    }.toPersistentList()
}

private fun PlatformType.toPlatformType(): CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType =
    when (this) {
        PlatformType.ANDROID -> CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType.ANDROID
        PlatformType.WASM -> CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType.WASM
        PlatformType.NATIVE -> CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType.NATIVE
        PlatformType.JS -> CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType.JS
        PlatformType.JVM -> CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType.JVM
    }

private fun CreateProjectState.SetupPlatforms.toSetupPlatforms(): CreateProjectWindowState.SetupPlatformsState =
    when (this) {
        is CreateProjectState.SetupPlatforms.NewProject ->
            CreateProjectWindowState.SetupPlatformsState(
                platforms = platforms.values.map { platform ->
                    CreateProjectWindowState.SetupPlatformsState.Platform(
                        platformType = platform.platformType.toPlatformType(),
                        properties = platform.properties.values.toProperties()
                    )
                }.toPersistentList(),
            )

        is CreateProjectState.SetupPlatforms.ExistingProject ->
            CreateProjectWindowState.SetupPlatformsState(
                platforms = platforms.values.map { platform ->
                    CreateProjectWindowState.SetupPlatformsState.Platform(
                        platformType = platform.platformType.toPlatformType(),
                        properties = platform.properties.values.toProperties()
                    )
                }.toPersistentList()
            )
    }

private fun Collection<Platform.Property>.toProperties(): ImmutableList<CreateProjectWindowState.SetupPlatformsState.Platform.Property> =
    map { property ->
        CreateProjectWindowState.SetupPlatformsState.Platform.Property(
            name =
        )
    }

private data class Context(
    val projectDirectoryPath: String,
    val stringsProvider: StringsProvider,
)