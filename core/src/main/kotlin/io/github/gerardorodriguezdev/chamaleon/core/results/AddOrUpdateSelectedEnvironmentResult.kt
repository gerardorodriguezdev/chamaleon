package io.github.gerardorodriguezdev.chamaleon.core.results

public sealed interface AddOrUpdateSelectedEnvironmentResult {
    public data object Success : AddOrUpdateSelectedEnvironmentResult

    public sealed interface Failure : AddOrUpdateSelectedEnvironmentResult {
        public val propertiesFilePath: String

        public data class InvalidFile(override val propertiesFilePath: String) : Failure
        public data class EnvironmentNameIsEmpty(override val propertiesFilePath: String) : Failure
        public data class Serialization(override val propertiesFilePath: String, val throwable: Throwable) : Failure
    }
}