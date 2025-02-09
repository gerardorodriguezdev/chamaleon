package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.EnvironmentsParserResult
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.EnvironmentsParserResult.Failure.Serialization
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData.LOCAL_ENVIRONMENT_NAME
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData.PRODUCTION_ENVIRONMENT_NAME
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DefaultEnvironmentsParserTest {
    @TempDir
    lateinit var environmentsDirectory: File

    private var environmentFileMatcher: (environmentFile: File) -> Boolean = { _ -> true }
    private var environmentNameExtractor: (environmentFile: File) -> String = { _ -> LOCAL_ENVIRONMENT_NAME }
    private var environmentFileNameExtractor: (environmentName: String) -> String =
        { environmentName -> environmentName }

    private val defaultEnvironmentsParser by lazy {
        DefaultEnvironmentsParser(
            environmentFileMatcher = environmentFileMatcher,
            environmentNameExtractor = environmentNameExtractor,
            environmentFileNameExtractor = environmentFileNameExtractor,
        )
    }

    @Test
    fun `GIVEN file not found WHEN environmentsParserResult THEN returns empty set`() {
        environmentFileMatcher = { false }
        val expectedEnvironmentsParserResult = EnvironmentsParserResult.Success(environments = setOf())

        val actualEnvironmentsParserResult = defaultEnvironmentsParser.environmentsParserResult(environmentsDirectory)

        assertEquals(expected = expectedEnvironmentsParserResult, actual = actualEnvironmentsParserResult)
    }

    @Test
    fun `GIVEN file empty WHEN environmentsParserResult THEN returns empty set`() {
        val expectedEnvironmentsParserResult = EnvironmentsParserResult.Success(environments = setOf())
        createEnvironmentsFile()

        val actualEnvironmentsParserResult = defaultEnvironmentsParser.environmentsParserResult(environmentsDirectory)

        assertEquals(expected = expectedEnvironmentsParserResult, actual = actualEnvironmentsParserResult)
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
            EnvironmentsParserResult.Success(environments = setOf(TestData.environment))
        createEnvironmentsFile(completeValidEnvironments)

        val actualEnvironmentsParserResult = defaultEnvironmentsParser.environmentsParserResult(environmentsDirectory)

        assertEquals(expected = expectedEnvironmentsParserResult, actual = actualEnvironmentsParserResult)
    }

    @Test
    fun `GIVEN environmentsDirectory not found WHEN addEnvironments THEN returns false`() {
        val result = defaultEnvironmentsParser.addEnvironments(
            environmentsDirectory = environmentsDirectory,
            environments = emptySet(),
        )

        assertFalse(result)
    }

    @Test
    fun `GIVEN environment file already exists WHEN addEnvironments THEN returns false`() {
        createEnvironmentsFile()

        val result = defaultEnvironmentsParser.addEnvironments(
            environmentsDirectory = environmentsDirectory,
            environments = setOf(
                Environment(
                    name = ENVIRONMENT_FILE_NAME,
                    platforms = setOf(TestData.jvmPlatform),
                )
            ),
        )

        assertFalse(result)
    }

    @Test
    fun `GIVEN environments WHEN addEnvironments THEN returns true`() {
        val result = defaultEnvironmentsParser.addEnvironments(
            environmentsDirectory = environmentsDirectory,
            environments = setOf(
                TestData.environment,
                TestData.environment.copy(name = "production"),
            ),
        )

        val localEnvironmentFile = File(environmentsDirectory, LOCAL_ENVIRONMENT_NAME)
        val localEnvironmentFileContent = localEnvironmentFile.readText()

        val productionEnvironmentFile = File(environmentsDirectory, PRODUCTION_ENVIRONMENT_NAME)
        val productionEnvironmentFileContent = productionEnvironmentFile.readText()

        assertTrue(result)
        assertEquals(expected = environmentWithoutOptionals, actual = localEnvironmentFileContent)
        assertEquals(expected = environmentWithoutOptionals, actual = productionEnvironmentFileContent)
    }

    private fun createEnvironmentsFile(content: String? = null) {
        if (!environmentsDirectory.exists()) {
            environmentsDirectory.mkdirs()
        }

        val environmentsFile = File(environmentsDirectory, ENVIRONMENT_FILE_NAME)
        environmentsFile.createNewFile()

        content?.let { content ->
            environmentsFile.writeText(content)
        }
    }

    companion object {
        const val ENVIRONMENT_FILE_NAME = "anyFileName"

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
                    "platformType": "js",
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
                    "platformType": "native",
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

        val environmentWithoutOptionals =
            //language=JSON
            """
                [
                  {
                    "platformType": "android",
                    "properties": [
                      {
                        "name": "DOMAIN",
                        "value": "www.domain.com"
                      },
                      {
                        "name": "IS_PRODUCTION",
                        "value": true
                      }
                    ]
                  },
                  {
                    "platformType": "wasm",
                    "properties": [
                      {
                        "name": "DOMAIN",
                        "value": "www.domain.com"
                      },
                      {
                        "name": "IS_PRODUCTION",
                        "value": true
                      }
                    ]
                  },
                  {
                    "platformType": "js",
                    "properties": [
                      {
                        "name": "DOMAIN",
                        "value": "www.domain.com"
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
                        "name": "DOMAIN",
                        "value": "www.domain.com"
                      },
                      {
                        "name": "IS_PRODUCTION",
                        "value": true
                      }
                    ]
                  },
                  {
                    "platformType": "native",
                    "properties": [
                      {
                        "name": "DOMAIN",
                        "value": "www.domain.com"
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