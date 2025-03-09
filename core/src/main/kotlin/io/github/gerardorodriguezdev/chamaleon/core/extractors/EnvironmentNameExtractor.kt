package io.github.gerardorodriguezdev.chamaleon.core.extractors

import io.github.gerardorodriguezdev.chamaleon.core.models.Project.Companion.ENVIRONMENT_FILE_SUFFIX
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingFile
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString

internal interface EnvironmentNameExtractor : (ExistingFile) -> NonEmptyString

internal object DefaultEnvironmentNameExtractor : EnvironmentNameExtractor {
    override fun invoke(environmentFile: ExistingFile): NonEmptyString =
        requireNotNull(environmentFile.name.removeSuffix(ENVIRONMENT_FILE_SUFFIX))
}