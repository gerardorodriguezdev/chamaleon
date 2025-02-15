package io.github.gerardorodriguezdev.chamaleon.core.entities.results

import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyType
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema

public sealed interface EnvironmentsProcessorResult {
    public data class Success(
        val environmentsDirectoryPath: String,
        val selectedEnvironmentName: String? = null,
        val environments: Set<Environment>,
        val schema: Schema,
    ) : EnvironmentsProcessorResult

    public sealed interface Failure : EnvironmentsProcessorResult {
        public data class EnvironmentsDirectoryNotFound(val environmentsDirectoryPath: String) : Failure

        public data class SchemaParsingError(val schemaParsingError: SchemaParserResult.Failure) : Failure

        public data class PropertiesParsingError(
            val propertiesParsingError: PropertiesParserResult.Failure
        ) : Failure

        public data class EnvironmentsParsingError(
            val environmentsParsingError: EnvironmentsParserResult.Failure,
        ) : Failure

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