package io.github.gerardorodriguezdev.chamaleon.core.testing.fakes

import io.github.gerardorodriguezdev.chamaleon.core.extractors.EnvironmentFileNameExtractor

internal class FakeEnvironmentFileNameExtractor(
    var environmentNameResult: String? = null,
) : EnvironmentFileNameExtractor {
    override fun invoke(environmentName: String): String = environmentNameResult ?: environmentName
}