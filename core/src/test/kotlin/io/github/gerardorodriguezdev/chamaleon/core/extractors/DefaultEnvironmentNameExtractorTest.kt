package io.github.gerardorodriguezdev.chamaleon.core.extractors

import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData.LOCAL_ENVIRONMENT_NAME
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData.PRODUCTION_ENVIRONMENT_NAME
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData.localEnvironmentFileName
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData.productionEnvironmentFileName
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import kotlin.test.assertEquals

class DefaultEnvironmentNameExtractorTest {

    @TempDir
    lateinit var directory: File

    private val environmentNameExtractor = DefaultEnvironmentNameExtractor()

    @ParameterizedTest
    @MethodSource("environmentNameExtractorTestData")
    fun `GIVEN environmentFile WHEN extracting environment name THEN extracts correctly`(
        expected: String,
        environmentFileName: String
    ) {
        val environmentFile = File(directory, environmentFileName)
        val actual = environmentNameExtractor(environmentFile)
        assertEquals(expected = expected, actual = actual)
    }

    companion object {
        @JvmStatic
        fun environmentNameExtractorTestData(): List<Arguments> =
            listOf(
                Arguments.of(LOCAL_ENVIRONMENT_NAME, localEnvironmentFileName),
                Arguments.of(PRODUCTION_ENVIRONMENT_NAME, productionEnvironmentFileName),
            )
    }
}