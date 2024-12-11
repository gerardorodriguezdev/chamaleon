package org.chamaleon.core.parsers

import org.chamaleon.core.parsers.EnvironmentsParser.EnvironmentsParserResult
import org.chamaleon.core.parsers.EnvironmentsParser.EnvironmentsParserResult.Failure.SerializationError
import org.chamaleon.core.testing.TestData
import org.chamaleon.core.testing.TestData.ENVIRONMENT_FILE
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DefaultEnvironmentsParserTest {
    @TempDir
    lateinit var directory: File

    private val defaultEnvironmentsParser by lazy { DefaultEnvironmentsParser(directory) }

    @Test
    fun `GIVEN file not found WHEN environmentsParserResult THEN returns empty set`() {
        val expectedEnvironmentsParserResult = EnvironmentsParserResult.Success(environments = setOf())

        val actualEnvironmentsParserResult = defaultEnvironmentsParser.environmentsParserResult()

        assertEquals(expectedEnvironmentsParserResult, actualEnvironmentsParserResult)
    }

    @Test
    fun `GIVEN file empty WHEN environmentsParserResult THEN returns empty set`() {
        val expectedEnvironmentsParserResult = EnvironmentsParserResult.Success(environments = setOf())
        createEnvironmentsFile()

        val actualEnvironmentsParserResult = defaultEnvironmentsParser.environmentsParserResult()

        assertEquals(expectedEnvironmentsParserResult, actualEnvironmentsParserResult)
    }

    @Test
    fun `GIVEN invalid environments json WHEN environmentsParserResult THEN returns error`() {
        createEnvironmentsFile(invalidEnvironmentsJson)

        val actualEnvironmentsParserResult = defaultEnvironmentsParser.environmentsParserResult()

        assertIs<SerializationError>(actualEnvironmentsParserResult)
    }

    @Test
    fun `GIVEN valid complete environments json WHEN environmentsParserResult THEN returns environments`() {
        val expectedEnvironmentsParserResult =
            EnvironmentsParserResult.Success(environments = setOf(TestData.validCompleteEnvironment))
        createEnvironmentsFile(completeValidEnvironmentsJson)

        val actualEnvironmentsParserResult = defaultEnvironmentsParser.environmentsParserResult()

        assertEquals(expectedEnvironmentsParserResult, actualEnvironmentsParserResult)
    }

    private fun createEnvironmentsFile(content: String? = null) {
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val environmentsFile = File(directory, ENVIRONMENT_FILE)
        environmentsFile.createNewFile()

        content?.let { content ->
            environmentsFile.writeText(content)
        }
    }

    companion object {
        val invalidEnvironmentsJson =
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

        val completeValidEnvironmentsJson =
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