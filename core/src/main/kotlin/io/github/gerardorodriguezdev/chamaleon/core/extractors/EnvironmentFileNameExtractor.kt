package io.github.gerardorodriguezdev.chamaleon.core.extractors

import io.github.gerardorodriguezdev.chamaleon.core.models.Project.Companion.environmentFileName
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString

internal interface EnvironmentFileNameExtractor : (NonEmptyString) -> NonEmptyString

internal object DefaultEnvironmentFileNameExtractor : EnvironmentFileNameExtractor {
    override fun invoke(environmentName: NonEmptyString): NonEmptyString = environmentFileName(environmentName)
}