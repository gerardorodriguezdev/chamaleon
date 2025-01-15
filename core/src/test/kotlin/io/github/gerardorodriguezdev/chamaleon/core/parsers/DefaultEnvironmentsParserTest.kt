package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.parsers.EnvironmentsParser.EnvironmentsParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.EnvironmentsParser.EnvironmentsParserResult.Failure.Serialization
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData.ENVIRONMENT_NAME
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DefaultEnvironmentsParserTest {
    @TempDir
    lateinit var environmentsDirectory: File

    private val defaultEnvironmentsParser by lazy {
        DefaultEnvironmentsParser(restrictedFileNames = listOf(RESTRICTED_FILE_NAME))
    }

    @Test
    fun `GIVEN file not found WHEN environmentsParserResult THEN returns empty set`() {
        val expectedEnvironmentsParserResult = EnvironmentsParserResult.Success(environments = setOf())

        val actualEnvironmentsParserResult = defaultEnvironmentsParser.environmentsParserResult(environmentsDirectory)

        assertEquals(expectedEnvironmentsParserResult, actualEnvironmentsParserResult)
    }

    @Test
    fun `GIVEN file empty WHEN environmentsParserResult THEN returns empty set`() {
        val expectedEnvironmentsParserResult = EnvironmentsParserResult.Success(environments = setOf())
        createEnvironmentsFile()

        val actualEnvironmentsParserResult = defaultEnvironmentsParser.environmentsParserResult(environmentsDirectory)

        assertEquals(expectedEnvironmentsParserResult, actualEnvironmentsParserResult)
    }

    @Test
    fun `GIVEN invalid environments WHEN environmentsParserResult THEN returns error`() {
        createEnvironmentsFile(invalidEnvironments)

        val actualEnvironmentsParserResult = defaultEnvironmentsParser.environmentsParserResult(environmentsDirectory)

        assertIs<Serialization>(actualEnvironmentsParserResult)
    }

    @Test
    fun `GIVEN valid complete environments WHEN environmentsParserResult THEN returns environments`() {
        val expectedEnvironmentsParserResult =
            EnvironmentsParserResult.Success(environments = setOf(TestData.validCompleteEnvironment))
        createEnvironmentsFile(completeValidEnvironments)

        val actualEnvironmentsParserResult = defaultEnvironmentsParser.environmentsParserResult(environmentsDirectory)

        assertEquals(expectedEnvironmentsParserResult, actualEnvironmentsParserResult)
    }

    private fun createEnvironmentsFile(content: String? = null) {
        if (!environmentsDirectory.exists()) {
            environmentsDirectory.mkdirs()
        }

        val environmentsFile = File(environmentsDirectory, ENVIRONMENT_FILE)
        environmentsFile.createNewFile()

        content?.let { content ->
            environmentsFile.writeText(content)
        }
    }

    companion object {
        const val ENVIRONMENT_FILE = "$ENVIRONMENT_NAME.chamaleon.json"

        const val RESTRICTED_FILE_NAME = "restricted"

        val invalidEnvironments =
            //language=json
            """
                [
                  {
                    "platformTyp": "android",
                    "propertie": [
                      {
                        "nam": "HOST",
                        "valu": "10.0.2.2"
                      }
                    ]
                  }
                ]
            """.trimIndent()

        val completeValidEnvironments =
            //language=json
            """
                [
                  {
                    "platformType": "wasm",
                    "properties": [
                      {
                        "name": "HOST",
                        "value": null
                      },
                      {
                        "name": "DOMAIN",
                        "value": "www.domain.com"
                      },
                      {
                        "name": "IS_DEBUG",
                        "value": null
                      },
                      {
                        "name": "IS_PRODUCTION",
                        "value": true
                      }
                    ]
                  },
                  {
                    "platformType": "android",
                    "properties": [
                      {
                        "name": "HOST",
                        "value": null
                      },
                      {
                        "name": "DOMAIN",
                        "value": "www.domain.com"
                      },
                      {
                        "name": "IS_DEBUG",
                        "value": null
                      },
                      {
                        "name": "IS_PRODUCTION",
                        "value": true
                      }
                    ]
                  },
                  {
                    "platformType": "jvm",
                    "properties": [
                      {
                        "name": "HOST",
                        "value": null
                      },
                      {
                        "name": "DOMAIN",
                        "value": "www.domain.com"
                      },
                      {
                        "name": "IS_DEBUG",
                        "value": null
                      },
                      {
                        "name": "IS_PRODUCTION",
                        "value": true
                      }
                    ]
                  },
                  {
                    "platformType": "ios",
                    "properties": [
                      {
                        "name": "HOST",
                        "value": null
                      },
                      {
                        "name": "DOMAIN",
                        "value": "www.domain.com"
                      },
                      {
                        "name": "IS_DEBUG",
                        "value": null
                      },
                      {
                        "name": "IS_PRODUCTION",
                        "value": true
                      }
                    ]
                  }
                ]
            """.trimIndent()
    }
}