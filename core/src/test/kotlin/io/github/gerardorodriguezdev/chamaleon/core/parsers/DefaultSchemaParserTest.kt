package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.results.SchemaParserResult
import io.github.gerardorodriguezdev.chamaleon.core.results.SchemaParserResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData
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
        val actualSchemaParserResult = defaultSchemaParser.schemaParserResult(schemaFile)

        assertIs<Failure.FileNotFound>(actualSchemaParserResult)
    }

    @Test
    fun `GIVEN schema empty WHEN schemaParserResult THEN returns failure`() {
        createSchemaFile()

        val actualSchemaParserResult = defaultSchemaParser.schemaParserResult(schemaFile)

        assertIs<Failure.FileIsEmpty>(actualSchemaParserResult)
    }

    @Test
    fun `GIVEN invalid schema file WHEN schemaParserResult THEN returns failure`() {
        createSchemaFile(invalidSchema)

        val schemaParserResult = defaultSchemaParser.schemaParserResult(schemaFile)

        assertIs<Failure.Serialization>(schemaParserResult)
    }

    @Test
    fun `GIVEN empty supported platform types WHEN schemaParserResult THEN returns failure`() {
        createSchemaFile(emptySupportedPlatformTypesSchema)

        val actualSchemaParserResult = defaultSchemaParser.schemaParserResult(schemaFile)

        assertIs<Failure.Serialization>(actualSchemaParserResult)
    }

    @Test
    fun `GIVEN empty property definitions WHEN schemaParserResult THEN returns failure`() {
        createSchemaFile(emptyPropertyDefinitionsSchema)

        val actualSchemaParserResult = defaultSchemaParser.schemaParserResult(schemaFile)

        assertIs<Failure.Serialization>(actualSchemaParserResult)
    }

    @Test
    fun `GIVEN invalid property definition WHEN schemaParserResult THEN returns failure`() {
        createSchemaFile(invalidPropertyDefinitionSchema)

        val actualSchemaParserResult = defaultSchemaParser.schemaParserResult(schemaFile)

        assertIs<Failure.Serialization>(actualSchemaParserResult)
    }

    @Test
    fun `GIVEN duplicated property definitions WHEN schemaParserResult THEN returns failure`() {
        createSchemaFile(duplicatedPropertyDefinitionSchema)

        val actualSchemaParserResult = defaultSchemaParser.schemaParserResult(schemaFile)

        assertIs<Failure.Serialization>(actualSchemaParserResult)
    }

    @Test
    fun `GIVEN complete valid schema file WHEN schemaParserResult THEN returns schemaDto`() {
        val expectedSchemaParserResult = SchemaParserResult.Success(TestData.schema)
        createSchemaFile(completeValidSchema)

        val actualSchemaParserResult = defaultSchemaParser.schemaParserResult(schemaFile)

        assertEquals(expected = expectedSchemaParserResult, actual = actualSchemaParserResult)
    }

    @Test
    fun `GIVEN valid schema file with supported platform types WHEN schemaParserResult THEN returns schemaDto`() {
        val expectedSchemaParserResult = SchemaParserResult.Success(TestData.schemaWithRestrictedPlatformTypes)
        createSchemaFile(validSchemaWithRestrictedPlatformTypes)

        val actualSchemaParserResult = defaultSchemaParser.schemaParserResult(schemaFile)

        assertEquals(expected = expectedSchemaParserResult, actual = actualSchemaParserResult)
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

        val emptySupportedPlatformTypesSchema =
            //language=json
            """
                {
                  "supportedPlatforms": [],
                  "propertyDefinitions": []
                }
            """.trimIndent()

        val emptyPropertyDefinitionsSchema =
            //language=json
            """
                {
                  "supportedPlatforms": ["android"],
                  "propertyDefinitions": []
                }
            """.trimIndent()

        val invalidPropertyDefinitionSchema =
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

        val duplicatedPropertyDefinitionSchema =
            //language=json
            """
                {
                  "supportedPlatforms": [
                    "android"
                  ],
                  "propertyDefinitions": [
                    {
                      "name": "host",
                      "propertyType": "String"
                    },
                    {
                      "name": "host",
                      "propertyType": "Boolean"
                    }
                  ]
                }
            """.trimIndent()

        val completeValidSchema =
            //language=json
            """
                {
                  "supportedPlatforms": [
                    "android",
                    "wasm",
                    "js",
                    "jvm",
                    "native"
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

        val validSchemaWithRestrictedPlatformTypes =
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