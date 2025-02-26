package io.github.gerardorodriguezdev.chamaleon.core.results

public sealed class PropertiesParserResult {
    internal data class Success(val selectedEnvironmentName: String? = null) : PropertiesParserResult()

    public sealed class Failure : PropertiesParserResult() {
        public abstract val propertiesFilePath: String

        public data class InvalidFile(override val propertiesFilePath: String) : Failure()
        public data class Serialization(override val propertiesFilePath: String, val throwable: Throwable) : Failure()
    }
}