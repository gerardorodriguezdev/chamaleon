package io.github.gerardorodriguezdev.chamaleon.core.entities.results

public sealed interface PropertiesParserResult {
    public data class Success(val selectedEnvironmentName: String? = null) : PropertiesParserResult
    public sealed interface Failure : PropertiesParserResult {
        public data class Serialization(val throwable: Throwable) : Failure
    }
}