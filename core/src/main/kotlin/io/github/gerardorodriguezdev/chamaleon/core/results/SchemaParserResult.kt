package io.github.gerardorodriguezdev.chamaleon.core.results

import io.github.gerardorodriguezdev.chamaleon.core.models.Schema

public sealed interface SchemaParserResult {
    public data class Success(val schema: Schema) : SchemaParserResult

    public sealed interface Failure : SchemaParserResult {
        public val schemaFilePath: String

        public data class FileNotFound(override val schemaFilePath: String) : Failure
        public data class FileIsEmpty(override val schemaFilePath: String) : Failure
        public data class EmptySupportedPlatforms(override val schemaFilePath: String) : Failure
        public data class EmptyPropertyDefinitions(override val schemaFilePath: String) : Failure
        public data class InvalidPropertyDefinition(override val schemaFilePath: String) : Failure
        public data class DuplicatedPropertyDefinition(override val schemaFilePath: String) : Failure
        public data class Serialization(override val schemaFilePath: String, val throwable: Throwable) : Failure
    }
}