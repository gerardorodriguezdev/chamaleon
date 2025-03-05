package io.github.gerardorodriguezdev.chamaleon.core.results

public sealed interface ProjectSerializationResult {
    public data object Success : ProjectSerializationResult
    public sealed interface Failure : ProjectSerializationResult {
        public val environmentsDirectoryPath: String

        public data class Serialization(override val environmentsDirectoryPath: String) : Failure
    }
}