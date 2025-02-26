package io.github.gerardorodriguezdev.chamaleon.core.results

import io.github.gerardorodriguezdev.chamaleon.core.models.Environment

public sealed class EnvironmentsParserResult {
    internal data class Success(val environmentsMap: Map<String, Environment>) : EnvironmentsParserResult()

    public sealed class Failure : EnvironmentsParserResult() {
        public abstract val environmentsDirectoryPath: String

        public data class FileIsEmpty(
            override val environmentsDirectoryPath: String,
            val environmentFilePath: String,
        ) : Failure()

        public data class EnvironmentNameEmpty(
            override val environmentsDirectoryPath: String,
            val environmentFilePath: String,
        ) : Failure()

        public data class Serialization(
            override val environmentsDirectoryPath: String,
            val throwable: Throwable,
        ) : Failure()
    }
}