package io.github.gerardorodriguezdev.chamaleon.core.results

import io.github.gerardorodriguezdev.chamaleon.core.models.Project

public sealed interface EnvironmentsProcessorResult {
    public data class Success(val project: Project) : EnvironmentsProcessorResult

    public sealed interface Failure : EnvironmentsProcessorResult {
        public val environmentsDirectoryPath: String

        public data class InvalidSchemaFile(
            override val environmentsDirectoryPath: String,
        ) : Failure

        public data class InvalidPropertiesFile(
            override val environmentsDirectoryPath: String,
        ) : Failure

        public data class SchemaParsing(
            override val environmentsDirectoryPath: String,
            val error: SchemaParserResult.Failure
        ) : Failure

        public data class PropertiesParsing(
            override val environmentsDirectoryPath: String,
            val error: PropertiesParserResult.Failure
        ) : Failure

        public data class EnvironmentsParsing(
            override val environmentsDirectoryPath: String,
            val error: EnvironmentsParserResult.Failure,
        ) : Failure

        public data class ProjectValidation(
            override val environmentsDirectoryPath: String,
            val error: ProjectValidationResult.Failure,
        ) : Failure
    }
}