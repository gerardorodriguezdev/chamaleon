package io.github.gerardorodriguezdev.chamaleon.core

import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyType

public sealed interface EnvironmentsProcessorResult {
    public data class Success(
        val environmentsDirectoryPath: String,
        val selectedEnvironmentName: String? = null,
        val environments: Set<Environment>,
    ) : EnvironmentsProcessorResult

    public sealed interface Failure : EnvironmentsProcessorResult {
        public data class EnvironmentsDirectoryNotFound(val environmentsDirectoryPath: String) : Failure

        //TODO: Specific error type
        public data class SchemaFileNotFound(val environmentsDirectoryPath: String) : Failure
        public data class SchemaFileIsEmpty(val environmentsDirectoryPath: String) : Failure
        public data class SchemaSerialization(val throwable: Throwable) : Failure
        public data class SchemaEmptySupportedPlatforms(val environmentsDirectoryPath: String) : Failure
        public data class SchemaEmptyPropertyDefinitions(val environmentsDirectoryPath: String) : Failure
        public data class SchemaInvalidPropertyDefinitions(val environmentsDirectoryPath: String) : Failure
        public data class SchemaDuplicatedPropertyDefinition(val environmentsDirectoryPath: String) : Failure

        public data class EnvironmentsSerialization(val throwable: Throwable) : Failure

        public data class PropertiesSerialization(val throwable: Throwable) : Failure

        public data class PlatformsNotEqualToSchema(val environmentName: String) : Failure
        public data class PropertiesNotEqualToSchema(
            val platformType: PlatformType,
            val environmentName: String
        ) : Failure

        public data class PropertyTypeNotMatchSchema(
            val propertyName: String,
            val platformType: PlatformType,
            val environmentName: String,
            val propertyType: PropertyType,
        ) : Failure

        public data class NullPropertyNotNullableOnSchema(
            val propertyName: String,
            val platformType: PlatformType,
            val environmentName: String,
        ) : Failure

        public data class SelectedEnvironmentInvalid(
            val selectedEnvironmentName: String,
            val environmentNames: String
        ) : Failure
    }
}