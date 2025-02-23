package io.github.gerardorodriguezdev.chamaleon.core.extractors

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor

internal interface EnvironmentFileNameExtractor : (String) -> String

internal class DefaultEnvironmentFileNameExtractor : EnvironmentFileNameExtractor {
    override fun invoke(environmentName: String): String = EnvironmentsProcessor.environmentFileName(environmentName)
}