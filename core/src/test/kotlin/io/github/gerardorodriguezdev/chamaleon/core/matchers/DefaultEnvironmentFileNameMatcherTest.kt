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

    @ParameterizedTest
    @MethodSource("environmentFileMatcherTestData")
    fun `GIVEN environmentFile WHEN invoked THEN matches correctly`(
        environmentFileName: String,
        expected: Boolean,
    ) {
        val environmentFile = File(directory, environmentFileName)
        val actual = DefaultEnvironmentFileNameMatcher(environmentFile)
        assertEquals(expected = expected, actual = actual)
    }

    companion object {
        @JvmStatic
        fun environmentFileMatcherTestData(): List<Arguments> =
            listOf(
                // Valid
                Arguments.of(localEnvironmentFileName, true),
                Arguments.of(productionEnvironmentFileName, true),

                // Invalid environment file name
                Arguments.of("local.environment.chamaleon.jso", false),
                Arguments.of("chamaleon.json", false),
                Arguments.of("local.json", false),
                Arguments.of("local.environment.json", false),
                Arguments.of("local.chamaleon.json", false),
                Arguments.of(EnvironmentsProcessor.ENVIRONMENT_FILE_SUFFIX, false),
                Arguments.of(EnvironmentsProcessor.SCHEMA_FILE, false),
                Arguments.of(EnvironmentsProcessor.PROPERTIES_FILE, false),
            )
    }
}