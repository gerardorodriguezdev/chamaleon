package io.github.gerardorodriguezdev.chamaleon.core.results

public sealed interface AddSchemaResult {
    public data object Success : AddSchemaResult

    public sealed interface Failure : AddSchemaResult {
        public val schemaFilePath: String

        public data class InvalidFile(override val schemaFilePath: String) : Failure
        public data class FileAlreadyPresent(override val schemaFilePath: String) : Failure
        public data class Serialization(override val schemaFilePath: String, val throwable: Throwable) : Failure
    }
}