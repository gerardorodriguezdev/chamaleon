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
        public val environmentsDirectoryPath: String

        public data class EnvironmentsDirectoryNotFound(
            override val environmentsDirectoryPath: String
        ) : Failure

        public data class SchemaParsingError(
            override val environmentsDirectoryPath: String,
            val schemaParsingError: SchemaParserResult.Failure
        ) : Failure

        public data class PropertiesParsingError(
            override val environmentsDirectoryPath: String,
            val propertiesParsingError: PropertiesParserResult.Failure
        ) : Failure

        public data class EnvironmentsParsingError(
            override val environmentsDirectoryPath: String,
            val environmentsParsingError: EnvironmentsParserResult.Failure,
        ) : Failure

        public data class PlatformsNotEqualToSchema(
            override val environmentsDirectoryPath: String,
            val environmentName: String
        ) : Failure

        public data class PropertiesNotEqualToSchema(
            override val environmentsDirectoryPath: String,
            val platformType: PlatformType,
            val environmentName: String
        ) : Failure

        public data class PropertyTypeNotMatchSchema(
            override val environmentsDirectoryPath: String,
            val propertyName: String,
            val platformType: PlatformType,
            val environmentName: String,
            val propertyType: PropertyType,
        ) : Failure

        public data class NullPropertyNotNullableOnSchema(
            override val environmentsDirectoryPath: String,
            val propertyName: String,
            val platformType: PlatformType,
            val environmentName: String,
        ) : Failure

        public data class SelectedEnvironmentInvalid(
            override val environmentsDirectoryPath: String,
            val selectedEnvironmentName: String,
            val environmentNames: String
        ) : Failure
    }
}