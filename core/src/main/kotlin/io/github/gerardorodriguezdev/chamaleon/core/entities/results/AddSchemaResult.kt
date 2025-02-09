package io.github.gerardorodriguezdev.chamaleon.core.entities.results

public sealed interface AddSchemaResult {
    public data object Success : AddSchemaResult
    public sealed interface Failure : AddSchemaResult {
        public data class EmptySupportedPlatforms(val path: String) : Failure
        public data class EmptyPropertyDefinitions(val path: String) : Failure
        public data class InvalidPropertyDefinition(val path: String) : Failure
        public data class DuplicatedPropertyDefinition(val path: String) : Failure
        public data class InvalidFile(val path: String) : Failure
        public data class FileAlreadyPresent(val path: String) : Failure
        public data class Serialization(val throwable: Throwable) : Failure
    }
}