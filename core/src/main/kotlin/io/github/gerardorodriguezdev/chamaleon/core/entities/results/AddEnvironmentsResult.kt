package io.github.gerardorodriguezdev.chamaleon.core.entities.results

public sealed interface AddEnvironmentsResult {
    public data object Success : AddEnvironmentsResult
    public sealed interface Failure : AddEnvironmentsResult {
        public data class InvalidDirectory(val path: String) : Failure
        public data class EmptyEnvironments(val path: String) : Failure
        public data class FileAlreadyPresent(val path: String) : Failure
        public data class EmptyPlatforms(val path: String) : Failure
        public data class InvalidPlatforms(val path: String) : Failure
        public data class EmptyEnvironmentName(val path: String) : Failure
        public data class Serialization(val throwable: Throwable) : Failure
    }
}