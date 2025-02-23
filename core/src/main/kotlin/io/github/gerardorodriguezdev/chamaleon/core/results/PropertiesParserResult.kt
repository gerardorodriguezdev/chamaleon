package io.github.gerardorodriguezdev.chamaleon.core.results

public sealed interface PropertiesParserResult {
    public data class Success(val selectedEnvironmentName: String? = null) : PropertiesParserResult

    public sealed interface Failure : PropertiesParserResult {
        public val propertiesFilePath: String

        public data class Serialization(override val propertiesFilePath: String, val throwable: Throwable) : Failure
    }
}