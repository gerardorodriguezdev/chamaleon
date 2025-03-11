package io.github.gerardorodriguezdev.chamaleon.gradle.plugin.mappers

import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectDeserializationResult
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectValidationResult

internal fun ProjectDeserializationResult.Failure.toErrorMessage(): String {
    val errorMessage = when (this) {
        is ProjectDeserializationResult.Failure.InvalidSchemaFile -> "Invalid schema file"
        is ProjectDeserializationResult.Failure.Deserialization -> "Deserialization error with error: '$error'"
        is ProjectDeserializationResult.Failure.ProjectValidation -> error.toErrorMessage()
    }

    return "$errorMessage at '$environmentsDirectoryPath'"
}

private fun ProjectValidationResult.Failure.toErrorMessage(): String =
    when (this) {
        is ProjectValidationResult.Failure.EnvironmentMissingPlatforms ->
            "Environment '$environmentName' is missing platforms '$missingPlatforms'"

        is ProjectValidationResult.Failure.PlatformMissingProperties ->
            "Platform '$platformType' on '$environmentName' is missing platforms '$missingPropertyNames'"

        is ProjectValidationResult.Failure.PropertyTypeNotEqualToPropertyDefinition ->
            "Platform type '$platformType' of property '$propertyName' on platform '$platformType' on " +
                    "environment '$environmentName' is different than property definition '$propertyDefinition'"

        is ProjectValidationResult.Failure.NullPropertyValueIsNotNullable ->
            "Property value on property '$propertyName' on platform '$platformType' on '$environmentName' is null but not nullable"

        is ProjectValidationResult.Failure.SelectedEnvironmentNotFound ->
            "Selected environment '$selectedEnvironmentName' is not present in any existing environment '$existingEnvironmentNames'"
    }