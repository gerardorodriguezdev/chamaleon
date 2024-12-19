package io.github.gerardorodriguezdev.chamaleon.core.parsers

import io.github.gerardorodriguezdev.chamaleon.core.parsers.SchemaParser.SchemaParserResult
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DefaultSchemaParserTest {

    @TempDir
    lateinit var directory: File

    private val schemaFile by lazy { File(directory, TestData.SCHEMA_FILE) }
    private val defaultSchemaParser by lazy { DefaultSchemaParser() }

    @Test
    fun `GIVEN no schema file WHEN schemaParserResult THEN returns failure`() {
        val expectedSchemaParserResult = SchemaParserResult.Failure.FileNotFound(directory.path)

        val actualSchemaParserResult = defaultSchemaParser.schemaParserResult(schemaFile)

        assertEquals(expectedSchemaParserResult, actualSchemaParserResult)
    }

    @Test
    fun `GIVEN schema empty WHEN schemaParserResult THEN returns failure`() {
        val expectedSchemaParserResult = SchemaParserResult.Failure.FileIsEmpty(directory.path)
        createSchemaFile()

        val actualSchemaParserResult = defaultSchemaParser.schemaParserResult(schemaFile)

        assertEquals(expectedSchemaParserResult, actualSchemaParserResult)
    }

    @Test
    fun `GIVEN invalid schema file WHEN schemaParserResult THEN returns failure`() {
        createSchemaFile(invalidSchema)

        val schemaParserResult = defaultSchemaParser.schemaParserResult(schemaFile)

        assertIs<SchemaParserResult.Failure.Serialization>(schemaParserResult)
    }

    @Test
    fun `GIVEN valid schema file WHEN schemaParserResult THEN returns schemaDto`() {
        val expectedSchemaParserResult = SchemaParserResult.Success(TestData.validCompleteSchema)
        createSchemaFile(completeValidSchema)

        val actualSchemaParserResult = defaultSchemaParser.schemaParserResult(schemaFile)

        assertEquals(expectedSchemaParserResult, actualSchemaParserResult)
    }

    private fun createSchemaFile(content: String? = null) {
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val schemaFile = File(directory, TestData.SCHEMA_FILE)
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
    }
}