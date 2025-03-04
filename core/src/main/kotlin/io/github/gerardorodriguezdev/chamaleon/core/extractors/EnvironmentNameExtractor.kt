package io.github.gerardorodriguezdev.chamaleon.core.extractors

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.ENVIRONMENT_FILE_SUFFIX
import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.ExistingFile
import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.NonEmptyString
import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.NonEmptyString.Companion.toNonEmptyString

internal interface EnvironmentNameExtractor : (ExistingFile) -> NonEmptyString

internal object DefaultEnvironmentNameExtractor : EnvironmentNameExtractor {
    override fun invoke(environmentFile: ExistingFile): NonEmptyString =
        environmentFile.toNonEmptyString().removeSuffix(ENVIRONMENT_FILE_SUFFIX)
}