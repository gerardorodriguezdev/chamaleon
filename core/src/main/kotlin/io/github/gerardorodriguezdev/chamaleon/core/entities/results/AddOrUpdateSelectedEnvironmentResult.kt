package io.github.gerardorodriguezdev.chamaleon.core.entities.results

public sealed interface AddOrUpdateSelectedEnvironmentResult {
    public data object Success : AddOrUpdateSelectedEnvironmentResult
    public sealed interface Failure : AddOrUpdateSelectedEnvironmentResult {
        public data class InvalidFile(val path: String) : Failure
        public data class EnvironmentNameIsEmpty(val path: String) : Failure
        public data class Serialization(val throwable: Throwable) : Failure
    }
}