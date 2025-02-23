package io.github.gerardorodriguezdev.chamaleon.core.testing.fakes

import io.github.gerardorodriguezdev.chamaleon.core.extractors.EnvironmentNameExtractor
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData.LOCAL_ENVIRONMENT_NAME
import java.io.File

internal class FakeEnvironmentNameExtractor(
    var environmentNameResult: String = LOCAL_ENVIRONMENT_NAME,
) : EnvironmentNameExtractor {
    override fun invoke(environmentFile: File): String = environmentNameResult
}