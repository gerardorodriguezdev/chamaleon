package io.github.gerardorodriguezdev.chamaleon.core.results

import io.github.gerardorodriguezdev.chamaleon.core.models.Platform.Property
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
            val schemaPlatformTypes: Set<PlatformType>,
            val environmentPlatformTypes: Set<PlatformType>,
        ) : Failure

        public data class PlatformMissingProperties(
            val environmentsDirectoryPath: String,
            val environmentName: String,
            val platformType: PlatformType,
            val schemaPropertyDefinitions: Set<PropertyDefinition>,
            val platformProperties: Set<Property>,
        ) : Failure

        public data class PropertyNotEqualToPropertyDefinition(
            val environmentsDirectoryPath: String,
            val platformType: PlatformType,
            val environmentName: String,
            val propertyName: String,
        ) : Failure

        public data class PropertyTypeNotEqualToPropertyDefinition(
            val environmentsDirectoryPath: String,
            val propertyName: String,
            val platformType: PlatformType,
            val environmentName: String,
            val propertyType: PropertyType,
        ) : Failure

        public data class NullPropertyNotNullable(
            val environmentsDirectoryPath: String,
            val propertyName: String,
            val platformType: PlatformType,
            val environmentName: String,
        ) : Failure

        public data class SelectedEnvironmentNotFound(
            val environmentsDirectoryPath: String,
            val selectedEnvironmentName: String,
            val environmentNames: String
        ) : Failure
    }
}