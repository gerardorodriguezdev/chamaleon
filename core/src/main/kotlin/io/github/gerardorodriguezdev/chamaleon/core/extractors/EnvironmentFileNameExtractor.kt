package io.github.gerardorodriguezdev.chamaleon.core.extractors

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString

internal interface EnvironmentFileNameExtractor : (NonEmptyString) -> NonEmptyString

internal object DefaultEnvironmentFileNameExtractor : EnvironmentFileNameExtractor {
    override fun invoke(environmentName: NonEmptyString): NonEmptyString =
        EnvironmentsProcessor.environmentFileName(environmentName)
}