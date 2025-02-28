package io.github.gerardorodriguezdev.chamaleon.core.results

import io.github.gerardorodriguezdev.chamaleon.core.models.Schema

public sealed interface SchemaParserResult {
    public data class Success(val schema: Schema) : SchemaParserResult

    public sealed interface Failure : SchemaParserResult {
        public val schemaFilePath: String

        public data class FileIsEmpty(override val schemaFilePath: String) : Failure
        public data class Serialization(override val schemaFilePath: String, val throwable: Throwable) : Failure
    }
}