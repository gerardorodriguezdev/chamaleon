package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser.PropertiesParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser.PropertiesParserResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DefaultPropertiesParserTest {
    @TempDir
    lateinit var directory: File

    private val propertiesFile by lazy { File(directory, TestData.PROPERTIES_FILE) }
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
        val expectedPropertiesParserResult = PropertiesParserResult.Success()

        val actualPropertiesParserResult = defaultPropertiesParser.propertiesParserResult(propertiesFile)

        assertEquals(expectedPropertiesParserResult, actualPropertiesParserResult)
    }

    @Test
    fun `GIVEN empty properties file WHEN propertiesParserResult THEN returns selected environment`() {
        val expectedPropertiesParserResult = PropertiesParserResult.Success()
        createPropertiesFile(emptyPropertiesFile)

        val actualPropertiesParserResult = defaultPropertiesParser.propertiesParserResult(propertiesFile)

        assertEquals(expectedPropertiesParserResult, actualPropertiesParserResult)
    }

    @Test
    fun `GIVEN valid properties file WHEN propertiesParserResult THEN returns selected environment`() {
        val expectedPropertiesParserResult = PropertiesParserResult.Success(SELECTED_ENVIRONMENT)
        createPropertiesFile(validPropertiesFile)

        val actualPropertiesParserResult = defaultPropertiesParser.propertiesParserResult(propertiesFile)

        assertEquals(expectedPropertiesParserResult, actualPropertiesParserResult)
    }

    private fun createPropertiesFile(content: String? = null) {
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val propertiesFile = File(directory, TestData.PROPERTIES_FILE)
        propertiesFile.createNewFile()

        content?.let { content ->
            propertiesFile.writeText(content)
        }
    }

    private companion object {
        const val SELECTED_ENVIRONMENT = "local"

        val invalidPropertiesFile =
            //language=json
            """
                {
                  "selectedEnvironmentNam": "local"
                }
            """.trimIndent()

        val emptyPropertiesFile = ""

        val validPropertiesFile =
            //language=json
            """
                {
                  "selectedEnvironmentName": "local"
                }
            """.trimIndent()
    }
}