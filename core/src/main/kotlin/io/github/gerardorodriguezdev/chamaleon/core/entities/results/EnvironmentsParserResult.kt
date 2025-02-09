package io.github.gerardorodriguezdev.chamaleon.core.entities.results

import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment

public sealed interface EnvironmentsParserResult {
    public data class Success(val environments: Set<Environment>) : EnvironmentsParserResult

    public sealed interface Failure : EnvironmentsParserResult {
        public data class NoEnvironmentsFound(val path: String) : Failure
        public data class InvalidEnvironment(val path: String) : Failure
        public data class EnvironmentNameEmpty(val path: String) : Failure
        public data class Serialization(val throwable: Throwable) : Failure
    }
}