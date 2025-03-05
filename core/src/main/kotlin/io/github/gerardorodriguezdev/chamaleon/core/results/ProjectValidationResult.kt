package io.github.gerardorodriguezdev.chamaleon.core.results

import io.github.gerardorodriguezdev.chamaleon.core.models.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.models.Project
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyType
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema.PropertyDefinition

public sealed interface ProjectValidationResult {
    public data class Success(val project: Project) : ProjectValidationResult
    public sealed interface Failure : ProjectValidationResult {
        public data class EnvironmentMissingPlatforms(
            val environmentsDirectoryPath: String,
            val environmentName: String,
            val missingPlatforms: Set<PlatformType>,
        ) : Failure

        public data class PlatformMissingProperties(
            val environmentsDirectoryPath: String,
            val environmentName: String,
            val platformType: PlatformType,
            val missingPropertyNames: Set<String>,
        ) : Failure

        public data class PropertyTypeNotEqualToPropertyDefinition(
            val environmentsDirectoryPath: String,
            val environmentName: String,
            val platformType: PlatformType,
            val propertyName: String,
            val propertyType: PropertyType,
            val propertyDefinition: PropertyDefinition,
        ) : Failure

        public data class NullPropertyValueIsNotNullable(
            val environmentsDirectoryPath: String,
            val environmentName: String,
            val propertyName: String,
            val platformType: PlatformType,
        ) : Failure

        public data class SelectedEnvironmentNotFound(
            val environmentsDirectoryPath: String,
            val selectedEnvironmentName: String,
            val existingEnvironmentNames: String
        ) : Failure
    }
}