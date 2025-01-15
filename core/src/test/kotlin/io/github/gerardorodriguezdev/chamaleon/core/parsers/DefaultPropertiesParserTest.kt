package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser.PropertiesParserResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser.PropertiesParserResult.Success
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.*

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

        assertEquals(expectedPropertiesParserResult, actualPropertiesParserResult)
    }

    @Test
    fun `GIVEN empty properties file WHEN propertiesParserResult THEN returns selected environment`() {
        val expectedPropertiesParserResult = Success()
        createPropertiesFile(EMPTY_PROPERTIES_FILE)

        val actualPropertiesParserResult = defaultPropertiesParser.propertiesParserResult(propertiesFile)

        assertEquals(expectedPropertiesParserResult, actualPropertiesParserResult)
    }

    @Test
    fun `GIVEN valid properties file WHEN propertiesParserResult THEN returns selected environment`() {
        val expectedPropertiesParserResult = Success(SELECTED_ENVIRONMENT)
        createPropertiesFile(validPropertiesFile)

        val actualPropertiesParserResult = defaultPropertiesParser.propertiesParserResult(propertiesFile)

        assertEquals(expectedPropertiesParserResult, actualPropertiesParserResult)
    }

    @Test
    fun `WHEN updateSelectedEnvironment THEN updates file`() {
        createPropertiesFile(EMPTY_PROPERTIES_FILE)

        val propertiesFileUpdated = defaultPropertiesParser.updateSelectedEnvironment(
            propertiesFile = propertiesFile,
            newSelectedEnvironment = SELECTED_ENVIRONMENT,
        )

        assertTrue { propertiesFileUpdated }
        val newSelectedEnvironmentName =
            defaultPropertiesParser.propertiesParserResult(propertiesFile).toSuccess().selectedEnvironmentName
        assertEquals(SELECTED_ENVIRONMENT, newSelectedEnvironmentName)
    }

    @Test
    fun `GIVEN selectedEnvironment WHEN updateSelectedEnvironment to null THEN empties file`() {
        createPropertiesFile(validPropertiesFile)

        val propertiesFileUpdated = defaultPropertiesParser.updateSelectedEnvironment(
            propertiesFile = propertiesFile,
            newSelectedEnvironment = null,
        )

        assertTrue { propertiesFileUpdated }
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

    private fun PropertiesParser.PropertiesParserResult.toSuccess(): Success = this as Success

    private companion object {
        const val SELECTED_ENVIRONMENT = "local"
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
    }
}