package io.github.gerardorodriguezdev.chamaleon.core.testing.fakes

import io.github.gerardorodriguezdev.chamaleon.core.matchers.EnvironmentFileNameMatcher
import java.io.File

internal class FakeEnvironmentFileNameMatcher(
    var environmentFileMatchResult: Boolean = true,
) : EnvironmentFileNameMatcher {
    override fun invoke(environmentFile: File): Boolean = environmentFileMatchResult
}