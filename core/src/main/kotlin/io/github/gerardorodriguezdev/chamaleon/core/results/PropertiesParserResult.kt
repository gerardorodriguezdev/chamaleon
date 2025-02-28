package io.github.gerardorodriguezdev.chamaleon.core.results

import io.github.gerardorodriguezdev.chamaleon.core.models.Properties

public sealed class PropertiesParserResult {
    internal data class Success(val properties: Properties) : PropertiesParserResult()

    public sealed class Failure : PropertiesParserResult() {
        public abstract val propertiesFilePath: String

        public data class Serialization(override val propertiesFilePath: String, val throwable: Throwable) : Failure()
    }
}