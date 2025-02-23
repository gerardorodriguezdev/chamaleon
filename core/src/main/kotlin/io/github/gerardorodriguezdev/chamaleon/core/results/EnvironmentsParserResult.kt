package io.github.gerardorodriguezdev.chamaleon.core.results

import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment

public sealed interface EnvironmentsParserResult {
    public data class Success(val environments: Set<Environment>) : EnvironmentsParserResult

    public sealed interface Failure : EnvironmentsParserResult {
        public val environmentsDirectoryPath: String

        public data class FileIsEmpty(
            override val environmentsDirectoryPath: String,
            val environmentFilePath: String,
        ) : Failure

        public data class EnvironmentNameEmpty(
            override val environmentsDirectoryPath: String,
            val environmentFilePath: String,
        ) : Failure

        public data class Serialization(
            override val environmentsDirectoryPath: String,
            val throwable: Throwable,
        ) : Failure
    }
}