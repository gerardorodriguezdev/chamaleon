package io.github.gerardorodriguezdev.chamaleon.core.results

public sealed interface UpdateProjectResult {
    public data object Success : UpdateProjectResult
    public sealed interface Failure : UpdateProjectResult {
        public val environmentsDirectoryPath: String

        public data class InvalidPropertiesFile(override val environmentsDirectoryPath: String) : Failure
        public data class InvalidSchemaFile(override val environmentsDirectoryPath: String) : Failure
        public data class InvalidEnvironmentFile(override val environmentsDirectoryPath: String) : Failure

        public data class Serialization(
            override val environmentsDirectoryPath: String, val throwable: Throwable
        ) : Failure
    }
}