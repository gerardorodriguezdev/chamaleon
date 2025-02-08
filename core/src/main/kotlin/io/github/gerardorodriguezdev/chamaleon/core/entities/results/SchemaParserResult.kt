package io.github.gerardorodriguezdev.chamaleon.core.entities.results

import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema

public sealed interface SchemaParserResult {
    public data class Success(val schema: Schema) : SchemaParserResult

    public sealed interface Failure : SchemaParserResult {
        public data class FileNotFound(val path: String) : Failure
        public data class FileIsEmpty(val path: String) : Failure
        public data class Serialization(val throwable: Throwable) : Failure
        public data class EmptySupportedPlatforms(val path: String) : Failure
        public data class EmptyPropertyDefinitions(val path: String) : Failure
        public data class InvalidPropertyDefinition(val path: String) : Failure
        public data class DuplicatedPropertyDefinition(val path: String) : Failure
    }
}