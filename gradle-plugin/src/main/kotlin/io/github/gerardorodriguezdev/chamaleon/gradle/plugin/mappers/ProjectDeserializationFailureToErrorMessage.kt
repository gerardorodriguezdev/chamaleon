package io.github.gerardorodriguezdev.chamaleon.gradle.plugin.mappers

import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectDeserializationResult
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectValidationResult

internal fun ProjectDeserializationResult.Failure.toErrorMessage(): String {
    val errorMessage = when (this) {
        is ProjectDeserializationResult.Failure.InvalidSchemaFile -> "Invalid schema file"
        is ProjectDeserializationResult.Failure.Deserialization -> "Deserialization error with error: '$throwable'"
        is ProjectDeserializationResult.Failure.ProjectValidation -> failure.toErrorMessage()
    }

    return "$errorMessage at directory '$environmentsDirectoryPath'"
}

@Suppress("Indentation")
private fun ProjectValidationResult.Failure.toErrorMessage(): String =
    when (this) {
        is ProjectValidationResult.Failure.EnvironmentMissingPlatforms ->
            "Environment '$environmentName' is missing platforms '$missingPlatforms'"

        is ProjectValidationResult.Failure.PlatformMissingProperties ->
            "Platform '$platformType' on environment '$environmentName' is missing properties '$missingPropertyNames'"

        is ProjectValidationResult.Failure.PropertyTypeNotEqualToPropertyDefinition ->
            "Platform type '$platformType' of property '$propertyName' on environment '$environmentName' " +
                "is different from expected type '$expectedPropertyType' on " +
                "property definition '$propertyDefinition'"

        is ProjectValidationResult.Failure.NullPropertyValueIsNotNullable ->
            "Property value on property '$propertyName' on platform '$platformType' on " +
                "environment '$environmentName' is null but not nullable"

        is ProjectValidationResult.Failure.SelectedEnvironmentNotFound ->
            "Selected environment '$selectedEnvironmentName' is not present in any existing " +
                "environment '$existingEnvironmentNames'"
    }