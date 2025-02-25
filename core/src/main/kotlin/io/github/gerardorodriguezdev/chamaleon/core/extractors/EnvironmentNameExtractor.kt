package io.github.gerardorodriguezdev.chamaleon.core.extractors

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.ENVIRONMENT_FILE_SUFFIX
import java.io.File

internal interface EnvironmentNameExtractor : (File) -> String

internal object DefaultEnvironmentNameExtractor : EnvironmentNameExtractor {
    override fun invoke(environmentFile: File): String = environmentFile.name.removeSuffix(ENVIRONMENT_FILE_SUFFIX)
}