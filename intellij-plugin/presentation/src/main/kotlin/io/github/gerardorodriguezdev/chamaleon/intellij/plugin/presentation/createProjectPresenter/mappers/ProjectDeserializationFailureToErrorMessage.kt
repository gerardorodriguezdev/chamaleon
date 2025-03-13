package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.mappers

import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectDeserializationResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectValidationResult
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsProvider

//TODO: Fix
internal fun Failure.toErrorMessage(stringsProvider: StringsProvider): String =
    when (this) {
        is Failure.Deserialization -> "${stringsProvider.string(StringsKeys.validField)} $environmentsDirectoryPath"
        is Failure.InvalidSchemaFile -> "${stringsProvider.string(StringsKeys.validField)} $environmentsDirectoryPath"
        is Failure.ProjectValidation -> error.toErrorMessage(stringsProvider)
    }

private fun ProjectValidationResult.Failure.toErrorMessage(stringsProvider: StringsProvider): String =
    when (this) {
        is ProjectValidationResult.Failure.SelectedEnvironmentNotFound -> ""
        is ProjectValidationResult.Failure.PlatformMissingProperties -> ""
        is ProjectValidationResult.Failure.PropertyTypeNotEqualToPropertyDefinition -> ""
        is ProjectValidationResult.Failure.EnvironmentMissingPlatforms -> ""
        is ProjectValidationResult.Failure.NullPropertyValueIsNotNullable -> ""
    }