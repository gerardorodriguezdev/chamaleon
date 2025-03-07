package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createEnvironmentPresenter.mappers

import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createEnvironmentPresenter.CreateEnvironmentState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createEnvironmentPresenter.CreateEnvironmentState.Step
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState.Platform.Property
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState.Platform.Property.PropertyValue
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsProvider
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.models.Field
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowState.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

internal fun CreateEnvironmentState.toWindowState(stringsProvider: StringsProvider): CreateEnvironmentWindowState =
    when (step) {
        Step.SETUP_ENVIRONMENT -> {
            SetupEnvironmentState(
                environmentsDirectoryPathField = Field(
                    value = environmentsDirectoryPath,
                    verification = environmentsDirectoryProcessResult.toVerification(stringsProvider),
                ),
                environmentNameField = Field(
                    value = environmentName,
                    verification = toEnvironmentNameVerification().toVerification(stringsProvider)
                )
            )
        }

        Step.SETUP_SCHEMA -> {
            SetupSchemaState(
                title = toTitle(stringsProvider),
                globalSupportedPlatforms = globalSupportedPlatforms.toPersistentList(),
                propertyDefinitions = propertyDefinitions.toPropertyDefinitions(stringsProvider),
            )
        }

        Step.SETUP_PROPERTIES -> {
            SetupPropertiesState(platforms = platforms.toPlatforms())
        }
    }

private fun EnvironmentsDirectoryProcessResult.toVerification(stringsProvider: StringsProvider): Field.Verification =
    when (this) {
        is EnvironmentsDirectoryProcessResult.Success -> Field.Verification.Valid
        is EnvironmentsDirectoryProcessResult.Loading -> Field.Verification.Loading
        is EnvironmentsDirectoryProcessResult.Failure -> toVerification(stringsProvider)
    }

private fun EnvironmentsDirectoryProcessResult.Failure.toVerification(stringsProvider: StringsProvider): Field.Verification =
    when (this) {
        is EnvironmentsDirectoryProcessResult.Failure.InvalidEnvironments -> Field.Verification.Invalid(
            stringsProvider.string(StringsKeys.invalidEnvironmentsFound)
        )

        is EnvironmentsDirectoryProcessResult.Failure.FileIsNotDirectory -> Field.Verification.Invalid(
            stringsProvider.string(StringsKeys.selectedFileNotDirectory)
        )

        is EnvironmentsDirectoryProcessResult.Failure.EnvironmentsDirectoryNotFound -> Field.Verification.Valid
        is EnvironmentsDirectoryProcessResult.Failure.SchemaFileNotFound -> Field.Verification.Valid
    }

private fun EnvironmentNameVerification.toVerification(stringsProvider: StringsProvider): Field.Verification =
    when (this) {
        EnvironmentNameVerification.VALID -> Field.Verification.Valid
        EnvironmentNameVerification.IS_EMPTY -> Field.Verification.Invalid(
            stringsProvider.string(StringsKeys.environmentNameEmpty)
        )

        EnvironmentNameVerification.IS_DUPLICATED -> Field.Verification.Invalid(
            stringsProvider.string(StringsKeys.environmentNameIsDuplicated)
        )
    }

private fun CreateEnvironmentState.toTitle(stringsProvider: StringsProvider): String =
    if (globalSupportedPlatforms.isEmpty() && propertyDefinitions.isEmpty()) {
        stringsProvider.string(StringsKeys.createTemplate)
    } else {
        stringsProvider.string(StringsKeys.selectedTemplate)
    }

private fun Set<PropertyDefinition>.toPropertyDefinitions(stringsProvider: StringsProvider): ImmutableList<SetupSchemaState.PropertyDefinition> =
    map { propertyDefinition ->
        propertyDefinition.toPropertyDefinition(stringsProvider)
    }.toPersistentList()

private fun PropertyDefinition.toPropertyDefinition(stringsProvider: StringsProvider): SetupSchemaState.PropertyDefinition =
    SetupSchemaState.PropertyDefinition(
        nameField = Field(
            value = name,
            verification = nameVerification(stringsProvider)
        ),
        propertyType = propertyType,
        nullable = nullable,
        supportedPlatforms = supportedPlatforms.toPersistentList(),
    )

// TODO: Mention duplicate
private fun PropertyDefinition.nameVerification(stringsProvider: StringsProvider): Field.Verification =
    if (name.isEmpty()) {
        Field.Verification.Invalid(stringsProvider.string(StringsKeys.propertyNameIsEmpty))
    } else {
        Field.Verification.Valid
    }

private fun Set<Platform>.toPlatforms(): ImmutableList<SetupPropertiesState.Platform> =
    map { platform ->
        SetupPropertiesState.Platform(
            platformType = platform.platformType,
            properties = platform.properties.toProperties()
        )
    }.toPersistentList()

private fun Set<Property>.toProperties(): ImmutableList<SetupPropertiesState.Platform.Property> =
    map { property ->
        property.toProperty()
    }.toPersistentList()

private fun Property.toProperty(): SetupPropertiesState.Platform.Property =
    SetupPropertiesState.Platform.Property(
        name = name,
        value = value.toPropertyValue(),
    )

internal fun PropertyValue.toPropertyValue(): SetupPropertiesState.Platform.Property.PropertyValue {
    return when (this) {
        is PropertyValue.StringProperty -> SetupPropertiesState.Platform.Property.PropertyValue.StringProperty(value)
        is PropertyValue.BooleanProperty -> SetupPropertiesState.Platform.Property.PropertyValue.BooleanProperty(value)
        is PropertyValue.NullableBooleanProperty ->
            SetupPropertiesState.Platform.Property.PropertyValue.NullableBooleanProperty(value)
    }
}