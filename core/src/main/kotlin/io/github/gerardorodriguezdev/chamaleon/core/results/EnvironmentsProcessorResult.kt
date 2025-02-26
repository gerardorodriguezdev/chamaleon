package io.github.gerardorodriguezdev.chamaleon.core.results

import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform.Property
import io.github.gerardorodriguezdev.chamaleon.core.models.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyType
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema.PropertyDefinition

public sealed interface EnvironmentsProcessorResult {
    public data class Success(
        val environmentsDirectoryPath: String,
        val selectedEnvironmentName: String? = null,
        val environmentsMap: Map<String, Environment>,
        val schema: Schema,
    ) : EnvironmentsProcessorResult

    public sealed interface Failure : EnvironmentsProcessorResult {
        public data class EnvironmentsDirectoryNotFound(
            val environmentsDirectoryPath: String
        ) : Failure

        public data class InvalidEnvironmentsDirectory(
            val environmentsDirectoryPath: String
        ) : Failure

        public data class SchemaParsingError(
            val environmentsDirectoryPath: String,
            val schemaParsingError: SchemaParserResult.Failure
        ) : Failure

        public data class PropertiesParsingError(
            val environmentsDirectoryPath: String,
            val propertiesParsingError: PropertiesParserResult.Failure
        ) : Failure

        public data class EnvironmentsParsingError(
            val environmentsParsingError: EnvironmentsParserResult.Failure,
        ) : Failure

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