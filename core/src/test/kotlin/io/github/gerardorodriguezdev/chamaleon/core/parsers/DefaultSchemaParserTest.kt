package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyType
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema.PropertyDefinition
import io.github.gerardorodriguezdev.chamaleon.core.parsers.SchemaParser.SchemaParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.SchemaParser.SchemaParserResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData.DOMAIN_PROPERTY_NAME
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData.HOST_PROPERTY_NAME
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DefaultSchemaParserTest {

    @TempDir
    lateinit var environmentsDirectory: File

    private val schemaFile by lazy { File(environmentsDirectory, "anyFileName") }
    private val defaultSchemaParser by lazy { DefaultSchemaParser() }

    @Test
    fun `GIVEN no schema file WHEN schemaParserResult THEN returns failure`() {
        val expectedSchemaParserResult = Failure.FileNotFound(environmentsDirectory.absolutePath)

        val actualSchemaParserResult = defaultSchemaParser.schemaParserResult(schemaFile)

        assertEquals(expectedSchemaParserResult, actualSchemaParserResult)
    }

    @Test
    fun `GIVEN schema empty WHEN schemaParserResult THEN returns failure`() {
        val expectedSchemaParserResult = Failure.FileIsEmpty(environmentsDirectory.absolutePath)
        createSchemaFile()

        val actualSchemaParserResult = defaultSchemaParser.schemaParserResult(schemaFile)

        assertEquals(expectedSchemaParserResult, actualSchemaParserResult)
    }

    @Test
    fun `GIVEN invalid schema file WHEN schemaParserResult THEN returns failure`() {
        createSchemaFile(invalidSchema)

        val schemaParserResult = defaultSchemaParser.schemaParserResult(schemaFile)

        assertIs<Failure.Serialization>(schemaParserResult)
    }

    @Test
    fun `GIVEN property contains unsupported platforms WHEN schemaParserResult THEN returns failure`() {
        createSchemaFile(invalidSchemaWithUnsupportedPlatforms)

        val schemaParserResult = defaultSchemaParser.schemaParserResult(schemaFile)

        assertIs<Failure.PropertyContainsUnsupportedPlatforms>(schemaParserResult)
    }

    @Test
    fun `GIVEN complete valid schema file WHEN schemaParserResult THEN returns schemaDto`() {
        val expectedSchemaParserResult = SchemaParserResult.Success(TestData.validCompleteSchema)
        createSchemaFile(completeValidSchema)

        val actualSchemaParserResult = defaultSchemaParser.schemaParserResult(schemaFile)

        assertEquals(expectedSchemaParserResult, actualSchemaParserResult)
    }

    @Test
    fun `GIVEN valid schema file with supported platforms WHEN schemaParserResult THEN returns schemaDto`() {
        val expectedSchemaParserResult = SchemaParserResult.Success(
            Schema(
                supportedPlatforms = setOf(
                    PlatformType.ANDROID,
                    PlatformType.JVM,
                ),
                propertyDefinitions = setOf(
                    PropertyDefinition(
                        name = HOST_PROPERTY_NAME,
                        propertyType = PropertyType.STRING,
                        nullable = true,
                        supportedPlatforms = setOf(PlatformType.ANDROID)
                    ),
                    PropertyDefinition(
                        name = DOMAIN_PROPERTY_NAME,
                        propertyType = PropertyType.STRING,
                        nullable = false,
                        supportedPlatforms = emptySet(),
                    ),
                )
            )
        )
        createSchemaFile(validSchemaWithSupportedPlatforms)

        val actualSchemaParserResult = defaultSchemaParser.schemaParserResult(schemaFile)

        assertEquals(expectedSchemaParserResult, actualSchemaParserResult)
    }

    private fun createSchemaFile(content: String? = null) {
        if (!environmentsDirectory.exists()) {
            environmentsDirectory.mkdirs()
        }

        schemaFile.createNewFile()

        content?.let { content ->
            schemaFile.writeText(content)
        }
    }

    private companion object {
        val invalidSchema =
            //language=json
            """
                {
                  "supported_platform": [],
                  "property_definition": []
                }
            """.trimIndent()

        val invalidSchemaWithUnsupportedPlatforms =
            //language=JSON
            """
                {
                  "supportedPlatforms": [
                    "android",
                    "jvm"
                  ],
                  "propertyDefinitions": [
                    {
                      "name": "HOST",
                      "propertyType": "String",
                      "nullable": true,
                      "supportedPlatforms": ["android", "wasm"]
                    },
                    {
                      "name": "DOMAIN",
                      "propertyType": "String"
                    }
                  ]
                }
            """.trimIndent()

        val completeValidSchema =
            //language=json
            """
                {
                  "supportedPlatforms": [
                    "wasm",
                    "android",
                    "jvm",
                    "ios"
                  ],
                  "propertyDefinitions": [
                    {
                      "name": "HOST",
                      "propertyType": "String",
                      "nullable": true
                    },
                    {
                      "name": "DOMAIN",
                      "propertyType": "String"
                    },
                    {
                      "name": "IS_DEBUG",
                      "propertyType": "Boolean",
                      "nullable": true
                    },
                    {
                      "name": "IS_PRODUCTION",
                      "propertyType": "Boolean"
                    }
                  ]
                }
            """.trimIndent()

        val validSchemaWithSupportedPlatforms =
            //language=JSON
            """
                {
                  "supportedPlatforms": [
                    "android",
                    "jvm"
                  ],
                  "propertyDefinitions": [
                    {
                      "name": "HOST",
                      "propertyType": "String",
                      "nullable": true,
                      "supportedPlatforms": ["android"]
                    },
                    {
                      "name": "DOMAIN",
                      "propertyType": "String"
                    }
                  ]
                }
            """.trimIndent()
    }
}