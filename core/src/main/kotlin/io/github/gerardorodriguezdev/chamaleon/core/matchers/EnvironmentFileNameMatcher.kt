package io.github.gerardorodriguezdev.chamaleon.core.matchers

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.ENVIRONMENT_FILE_SUFFIX
import java.io.File

internal interface EnvironmentFileNameMatcher : (File) -> Boolean

internal class DefaultEnvironmentFileNameMatcher : EnvironmentFileNameMatcher {
    override fun invoke(environmentFile: File): Boolean =
        environmentFile.name != ENVIRONMENT_FILE_SUFFIX && environmentFile.name.endsWith(ENVIRONMENT_FILE_SUFFIX)
}