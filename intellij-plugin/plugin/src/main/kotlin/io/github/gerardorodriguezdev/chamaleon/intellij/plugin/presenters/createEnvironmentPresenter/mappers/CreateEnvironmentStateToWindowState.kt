package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.mappers

import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState.EnvironmentsDirectoryProcessResult
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState.Step
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsProvider
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.models.Field
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowState.SetupEnvironmentState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowState.SetupSchemaState
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
                title = if (schema.isEmpty()) {
                    stringsProvider.string(StringsKeys.createTemplate)
                } else {
                    stringsProvider.string(StringsKeys.updateTemplate)
                },
                supportedPlatforms = schema.supportedPlatforms.toPersistentList(),
                propertyDefinitions = schema.propertyDefinitions.toPropertyDefinitions(stringsProvider),
            )
        }
    }

private fun EnvironmentsDirectoryProcessResult.toVerification(stringsProvider: StringsProvider): Field.Verification =
    when (this) {
        is EnvironmentsDirectoryProcessResult.Success -> Field.Verification.Valid
        is EnvironmentsDirectoryProcessResult.Loading -> Field.Verification.Loading
        is EnvironmentsDirectoryProcessResult.Failure -> when (this) {
            is EnvironmentsDirectoryProcessResult.Failure.InvalidEnvironments -> Field.Verification.Invalid(
                stringsProvider.string(StringsKeys.invalidEnvironmentsFound)
            )

            is EnvironmentsDirectoryProcessResult.Failure.FileIsNotDirectory -> Field.Verification.Invalid(
                stringsProvider.string(StringsKeys.selectedFileNotDirectory)
            )

            is EnvironmentsDirectoryProcessResult.Failure.EnvironmentsDirectoryNotFound -> Field.Verification.Valid
            is EnvironmentsDirectoryProcessResult.Failure.SchemaFileNotFound -> Field.Verification.Valid
        }
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

private fun Set<Schema.PropertyDefinition>.toPropertyDefinitions(stringsProvider: StringsProvider): ImmutableList<SetupSchemaState.PropertyDefinition> =
    map { propertyDefinition ->
        propertyDefinition.toPropertyDefinition(stringsProvider)
    }.toPersistentList()

private fun Schema.PropertyDefinition.toPropertyDefinition(stringsProvider: StringsProvider): SetupSchemaState.PropertyDefinition =
    SetupSchemaState.PropertyDefinition(
        nameField = Field(
            value = name,
            verification = if (name.isEmpty()) {
                Field.Verification.Invalid(stringsProvider.string(StringsKeys.propertyNameIsEmpty))
            } else {
                Field.Verification.Valid
            }
        ),
        propertyType = propertyType,
        nullable = nullable,
        supportedPlatforms = supportedPlatforms.toPersistentList(),
    )