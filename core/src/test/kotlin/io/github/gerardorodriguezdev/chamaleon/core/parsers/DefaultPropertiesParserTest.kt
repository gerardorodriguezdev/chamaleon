package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.results.AddOrUpdateSelectedEnvironmentResult
import io.github.gerardorodriguezdev.chamaleon.core.results.PropertiesParserResult
import io.github.gerardorodriguezdev.chamaleon.core.results.PropertiesParserResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.results.PropertiesParserResult.Success
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class DefaultPropertiesParserTest {
    @TempDir
    lateinit var environmentsDirectory: File

    private val propertiesFile by lazy { File(environmentsDirectory, "anyFileName") }
    private val defaultPropertiesParser by lazy { DefaultPropertiesParser() }

    @Test
    fun `GIVEN invalid properties file WHEN propertiesParserResult THEN returns failure`() {
        createPropertiesFile(invalidPropertiesFile)

        val actualPropertiesParserResult = defaultPropertiesParser.propertiesParserResult(propertiesFile)
        actualPropertiesParserResult as Failure

        assertIs<Failure>(actualPropertiesParserResult)
    }

    @Test
    fun `GIVEN no properties file WHEN propertiesParserResult THEN returns success`() {
        val expectedPropertiesParserResult = Success()

        val actualPropertiesParserResult = defaultPropertiesParser.propertiesParserResult(propertiesFile)

        assertEquals(expected = expectedPropertiesParserResult, actual = actualPropertiesParserResult)
    }

    @Test
    fun `GIVEN empty properties file WHEN propertiesParserResult THEN returns selected environment`() {
        val expectedPropertiesParserResult = Success()
        createPropertiesFile(EMPTY_PROPERTIES_FILE)

        val actualPropertiesParserResult = defaultPropertiesParser.propertiesParserResult(propertiesFile)

        assertEquals(expected = expectedPropertiesParserResult, actual = actualPropertiesParserResult)
    }

    @Test
    fun `GIVEN valid properties file WHEN propertiesParserResult THEN returns selected environment`() {
        val expectedPropertiesParserResult = Success(TestData.LOCAL_ENVIRONMENT_NAME)
        createPropertiesFile(validPropertiesFile)

        val actualPropertiesParserResult = defaultPropertiesParser.propertiesParserResult(propertiesFile)

        assertEquals(expected = expectedPropertiesParserResult, actual = actualPropertiesParserResult)
    }

    @Test
    fun `GIVEN valid properties file with no selected environment WHEN propertiesParserResult THEN returns null`() {
        val expectedPropertiesParserResult = Success()
        createPropertiesFile(validPropertiesFileWithNoSelectedEnvironment)

        val actualPropertiesParserResult = defaultPropertiesParser.propertiesParserResult(propertiesFile)

        assertEquals(expected = expectedPropertiesParserResult, actual = actualPropertiesParserResult)
    }

    @Test
    fun `WHEN addOrUpdateSelectedEnvironment with environment THEN updates file`() {
        createPropertiesFile(EMPTY_PROPERTIES_FILE)

        val addOrUpdateSelectedEnvironmentResult = defaultPropertiesParser.addOrUpdateSelectedEnvironment(
            propertiesFile = propertiesFile,
            newSelectedEnvironment = TestData.LOCAL_ENVIRONMENT_NAME,
        )

        assertEquals(
            expected = AddOrUpdateSelectedEnvironmentResult.Success,
            actual = addOrUpdateSelectedEnvironmentResult
        )
        val newSelectedEnvironmentName =
            defaultPropertiesParser.propertiesParserResult(propertiesFile).toSuccess().selectedEnvironmentName
        assertEquals(expected = TestData.LOCAL_ENVIRONMENT_NAME, actual = newSelectedEnvironmentName)
    }

    @Test
    fun `WHEN addOrUpdateSelectedEnvironment without environment THEN updates file`() {
        createPropertiesFile(EMPTY_PROPERTIES_FILE)

        val addOrUpdateSelectedEnvironmentResult = defaultPropertiesParser.addOrUpdateSelectedEnvironment(
            propertiesFile = propertiesFile,
            newSelectedEnvironment = null,
        )

        assertEquals(
            expected = AddOrUpdateSelectedEnvironmentResult.Success,
            actual = addOrUpdateSelectedEnvironmentResult
        )
        val newSelectedEnvironmentName =
            defaultPropertiesParser.propertiesParserResult(propertiesFile).toSuccess().selectedEnvironmentName
        assertNull(newSelectedEnvironmentName)
    }

    @Test
    fun `GIVEN selectedEnvironment WHEN addOrUpdateSelectedEnvironment to null THEN empties file`() {
        createPropertiesFile(validPropertiesFile)

        val addOrUpdateSelectedEnvironmentResult = defaultPropertiesParser.addOrUpdateSelectedEnvironment(
            propertiesFile = propertiesFile,
            newSelectedEnvironment = null,
        )

        assertEquals(
            expected = AddOrUpdateSelectedEnvironmentResult.Success,
            actual = addOrUpdateSelectedEnvironmentResult
        )
        val newSelectedEnvironmentName =
            defaultPropertiesParser.propertiesParserResult(propertiesFile).toSuccess().selectedEnvironmentName
        assertNull(newSelectedEnvironmentName)
    }

    private fun createPropertiesFile(content: String? = null) {
        if (!environmentsDirectory.exists()) {
            environmentsDirectory.mkdirs()
        }

        propertiesFile.createNewFile()

        content?.let { content ->
            propertiesFile.writeText(content)
        }
    }

    private companion object {
        const val EMPTY_PROPERTIES_FILE = ""

        val invalidPropertiesFile =
            //language=json
            """
                {
                  "selectedEnvironmentNam": "local"
                }
            """.trimIndent()

        val validPropertiesFile =
            //language=json
            """
                {
                  "selectedEnvironmentName": "local"
                }
            """.trimIndent()

        val validPropertiesFileWithNoSelectedEnvironment =
            //language=json
            """
                {
                  "selectedEnvironmentName": null
                }
            """.trimIndent()

        fun PropertiesParserResult.toSuccess(): Success = this as Success
    }
}