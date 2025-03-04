package io.github.gerardorodriguezdev.chamaleon.core.results

import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore

public sealed class EnvironmentsParserResult {
    internal data class Success(val environments: NonEmptyKeySetStore<String, Environment>?) :
        EnvironmentsParserResult()

    public sealed class Failure : EnvironmentsParserResult() {
        public abstract val environmentsDirectoryPath: String

        public data class FileIsEmpty(
            override val environmentsDirectoryPath: String,
            val environmentFilePath: String,
        ) : Failure()

        public data class InvalidEnvironmentFile(
            override val environmentsDirectoryPath: String,
            val environmentFilePath: String,
        ) : Failure()

        public data class Serialization(
            override val environmentsDirectoryPath: String,
            val throwable: Throwable,
        ) : Failure()
    }
}