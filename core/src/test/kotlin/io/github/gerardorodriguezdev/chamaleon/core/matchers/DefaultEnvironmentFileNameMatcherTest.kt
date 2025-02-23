package io.github.gerardorodriguezdev.chamaleon.core.matchers

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData.localEnvironmentFileName
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData.productionEnvironmentFileName
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import kotlin.test.assertEquals

class DefaultEnvironmentFileNameMatcherTest {
    @TempDir
    lateinit var directory: File

    private val environmentFileNameMatcher = DefaultEnvironmentFileNameMatcher()

    @ParameterizedTest
    @MethodSource("environmentFileMatcherTestData")
    fun `GIVEN environment file name WHEN matching THEN matches correctly`(
        expected: Boolean,
        environmentFileName: String,
    ) {
        val environmentFile = File(directory, environmentFileName)
        val actual = environmentFileNameMatcher(environmentFile)
        assertEquals(expected = expected, actual = actual)
    }

    companion object {
        @JvmStatic
        fun environmentFileMatcherTestData(): List<Arguments> =
            listOf(
                // Valid
                Arguments.of(true, localEnvironmentFileName),
                Arguments.of(true, productionEnvironmentFileName),

                // Invalid environment file name
                Arguments.of(false, "local.environment.chamaleon.jso"),
                Arguments.of(false, "chamaleon.json"),
                Arguments.of(false, "local.json"),
                Arguments.of(false, "local.environment.json"),
                Arguments.of(false, "local.chamaleon.json"),
                Arguments.of(false, EnvironmentsProcessor.ENVIRONMENT_FILE_SUFFIX),
                Arguments.of(false, EnvironmentsProcessor.SCHEMA_FILE),
                Arguments.of(false, EnvironmentsProcessor.PROPERTIES_FILE),
            )
    }
}