package io.github.gerardorodriguezdev.chamaleon.core.results

public sealed interface AddEnvironmentsResult {
    public data object Success : AddEnvironmentsResult

    public sealed interface Failure : AddEnvironmentsResult {
        public val environmentsDirectoryPath: String

        public data class InvalidDirectory(override val environmentsDirectoryPath: String) : Failure
        public data class FileAlreadyPresent(
            override val environmentsDirectoryPath: String,
            val environmentName: String,
        ) : Failure

        public data class EmptyEnvironments(override val environmentsDirectoryPath: String) : Failure

        public data class Serialization(
            override val environmentsDirectoryPath: String,
            val throwable: Throwable,
        ) : Failure
    }
}