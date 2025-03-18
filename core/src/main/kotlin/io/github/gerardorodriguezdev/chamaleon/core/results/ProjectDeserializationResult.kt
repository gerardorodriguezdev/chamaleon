package io.github.gerardorodriguezdev.chamaleon.core.results

import io.github.gerardorodriguezdev.chamaleon.core.models.Project

public sealed interface ProjectDeserializationResult {
    public data class Success(val project: Project) : ProjectDeserializationResult

    public sealed interface Failure : ProjectDeserializationResult {
        public val environmentsDirectoryPath: String

        public data class InvalidSchemaFile(
            override val environmentsDirectoryPath: String,
        ) : Failure

        public data class Deserialization(
            override val environmentsDirectoryPath: String,
            val throwable: Throwable,
        ) : Failure

        public data class ProjectValidation(
            override val environmentsDirectoryPath: String,
            val failure: ProjectValidationResult.Failure,
        ) : Failure
    }
}