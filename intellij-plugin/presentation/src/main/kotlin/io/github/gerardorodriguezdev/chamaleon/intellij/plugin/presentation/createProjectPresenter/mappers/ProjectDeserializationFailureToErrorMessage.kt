package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.mappers

import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectDeserializationResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectValidationResult
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsProvider

internal fun Failure.toErrorMessage(stringsProvider: StringsProvider): String =
    when (this) {
        is Failure.Deserialization ->
            stringsProvider.string(
                StringsKeys.deserializationError(
                    errorMessage = throwable.toString(),
                    environmentsDirectoryPath = environmentsDirectoryPath,
                )
            )

        is Failure.InvalidSchemaFile ->
            stringsProvider.string(
                StringsKeys.invalidSchemaFile(environmentsDirectoryPath)
            )

        is Failure.ProjectValidation -> failure.toErrorMessage(stringsProvider)
    }

private fun ProjectValidationResult.Failure.toErrorMessage(stringsProvider: StringsProvider): String =
    when (this) {
        is ProjectValidationResult.Failure.SelectedEnvironmentNotFound ->
            stringsProvider.string(
                StringsKeys.selectedEnvironmentNotFound(
                    selectedEnvironmentName = selectedEnvironmentName,
                    existingEnvironmentNames = existingEnvironmentNames,
                    environmentsDirectoryPath = environmentsDirectoryPath,
                )
            )

        is ProjectValidationResult.Failure.PlatformMissingProperties ->
            stringsProvider.string(
                StringsKeys.platformMissingProperties(
                    platformType = platformType.name,
                    environmentName = environmentName,
                    missingPropertyNames = missingPropertyNames.toString(),
                    environmentsDirectoryPath = environmentsDirectoryPath,
                )
            )

        is ProjectValidationResult.Failure.PropertyTypeNotEqualToPropertyDefinition ->
            stringsProvider.string(
                StringsKeys.propertyTypeNotEqualToPropertyDefinition(
                    platformType = platformType.name,
                    propertyName = propertyName,
                    environmentName = environmentName,
                    expectedPropertyType = expectedPropertyType.name,
                    propertyDefinition = propertyDefinition.name.value,
                    environmentsDirectoryPath = environmentsDirectoryPath,
                )
            )

        is ProjectValidationResult.Failure.EnvironmentMissingPlatforms ->
            stringsProvider.string(
                StringsKeys.environmentMissingPlatforms(
                    environmentName = environmentName,
                    missingPlatforms = missingPlatforms.toString(),
                    environmentsDirectoryPath = environmentsDirectoryPath,
                )
            )

        is ProjectValidationResult.Failure.NullPropertyValueIsNotNullable ->
            stringsProvider.string(
                StringsKeys.nullPropertyValueIsNotNullable(
                    propertyName = propertyName,
                    platformType = platformType.name,
                    environmentName = environmentName,
                    environmentsDirectoryPath = environmentsDirectoryPath,
                )
            )
    }