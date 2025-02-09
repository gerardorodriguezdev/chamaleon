package io.github.gerardorodriguezdev.chamaleon.core.serializers

import io.github.gerardorodriguezdev.chamaleon.core.dtos.SchemaDto
import io.github.gerardorodriguezdev.chamaleon.core.dtos.SchemaDto.PropertyDefinitionDto
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyType
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData.HOST_PROPERTY_NAME
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SchemaDtoSerializerTest {

    @Nested
    inner class Serialize {
        @Test
        fun `GIVEN empty supported platforms WHEN serialize THEN throws error`() {
            val schemaDto = SchemaDto(supportedPlatforms = emptySet(), propertyDefinitionsDtos = emptySet())

            assertThrows<SerializationException> {
                Json.encodeToJsonElement(schemaDto)
            }
        }

        @Test
        fun `GIVEN empty property definitions dtos WHEN serialize THEN throws error`() {
            val schemaDto = SchemaDto(supportedPlatforms = TestData.allPlatforms, propertyDefinitionsDtos = emptySet())

            assertThrows<SerializationException> {
                Json.encodeToJsonElement(schemaDto)
            }
        }

        @Test
        fun `GIVEN property definitions dtos with unsupported platforms WHEN serialize THEN throws error`() {
            val schemaDto = SchemaDto(
                supportedPlatforms = setOf(PlatformType.ANDROID),
                propertyDefinitionsDtos = setOf(
                    PropertyDefinitionDto(
                        name = HOST_PROPERTY_NAME,
                        propertyType = PropertyType.STRING,
                        supportedPlatforms = setOf(PlatformType.JVM),
                    )
                ),
            )

            assertThrows<SerializationException> {
                Json.encodeToJsonElement(schemaDto)
            }
        }

        @Test
        fun `GIVEN property definitions dtos with empty name WHEN serialize THEN throws error`() {
            val schemaDto = SchemaDto(
                supportedPlatforms = setOf(PlatformType.ANDROID),
                propertyDefinitionsDtos = setOf(
                    PropertyDefinitionDto(
                        name = "",
                        propertyType = PropertyType.STRING,
                    )
                ),
            )

            assertThrows<SerializationException> {
                Json.encodeToJsonElement(schemaDto)
            }
        }

        @Test
        fun `GIVEN duplicated property definitions dtos WHEN serialize THEN throws error`() {
            val schemaDto = SchemaDto(
                supportedPlatforms = setOf(PlatformType.ANDROID),
                propertyDefinitionsDtos = setOf(
                    PropertyDefinitionDto(
                        name = HOST_PROPERTY_NAME,
                        propertyType = PropertyType.STRING,
                    ),
                    PropertyDefinitionDto(
                        name = HOST_PROPERTY_NAME,
                        propertyType = PropertyType.BOOLEAN,
                    )
                ),
            )

            assertThrows<SerializationException> {
                Json.encodeToJsonElement(schemaDto)
            }
        }

        @Test
        @Suppress("MaximumLineLength")
        fun `GIVEN valid schema dto WHEN serialize THEN returns json`() {
            //language=json
            val expectedJson =
                """{"supportedPlatforms":["android","jvm"],"propertyDefinitions":[{"name":"HOST","propertyType":"String","nullable":true,"supportedPlatforms":["jvm"]}]}""".trimIndent()

            val actualJson = Json.encodeToString(validSchemaDto)

            assertEquals(expected = expectedJson, actual = actualJson)
        }
    }

    @Nested
    inner class Deserialize {

        @Test
        fun `GIVEN empty supported platforms WHEN deserialize THEN throws error`() {
            val json =
                //language=json
                """
                {
                    "supportedPlatforms": [],
                    "propertyDefinitions": []
                }
                """.trimIndent()

            assertThrows<SerializationException> {
                Json.decodeFromString<SchemaDto>(json)
            }
        }

        @Test
        fun `GIVEN empty property definitions WHEN deserialize THEN throws error`() {
            val json =
                //language=json
                """
                {
                    "supportedPlatforms": ["android"],
                    "propertyDefinitions": []
                }
                """.trimIndent()

            assertThrows<SerializationException> {
                Json.decodeFromString<SchemaDto>(json)
            }
        }

        @Test
        fun `GIVEN empty property definitions name WHEN deserialize THEN throws error`() {
            val json =
                //language=json
                """
                    {
                      "supportedPlatforms": [
                        "android"
                      ],
                      "propertyDefinitions": [
                        {
                          "name": "",
                          "propertyType": "String"
                        }
                      ]
                    }
                """.trimIndent()

            assertThrows<SerializationException> {
                Json.decodeFromString<SchemaDto>(json)
            }
        }

        @Test
        fun `GIVEN duplicated property definitions name WHEN deserialize THEN throws error`() {
            val json =
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

            assertThrows<SerializationException> {
                Json.decodeFromString<SchemaDto>(json)
            }
        }

        @Test
        fun `GIVEN unsupported platforms on property definitions name WHEN deserialize THEN throws error`() {
            val json =
                //language=json
                """
                    {
                      "supportedPlatforms": [
                        "android"
                      ],
                      "propertyDefinitions": [
                        {
                          "name": "host",
                          "propertyType": "String",
                          "supportedPlatforms": ["wasm"]
                        }
                      ]
                    }
                """.trimIndent()

            assertThrows<SerializationException> {
                Json.decodeFromString<SchemaDto>(json)
            }
        }

        @Test
        fun `GIVEN valid WHEN deserialize THEN returns nullable string`() {
            val json =
                //language=json
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
                      "supportedPlatforms": [
                        "jvm"
                      ]
                    }
                  ]
                }
                """.trimIndent()

            val actualSchemaDto = Json.decodeFromString<SchemaDto>(json)

            assertEquals(expected = validSchemaDto, actual = actualSchemaDto)
        }
    }

    companion object {
        val validSchemaDto = SchemaDto(
            supportedPlatforms = setOf(PlatformType.ANDROID, PlatformType.JVM),
            propertyDefinitionsDtos = setOf(
                PropertyDefinitionDto(
                    name = HOST_PROPERTY_NAME,
                    propertyType = PropertyType.STRING,
                    nullable = true,
                    supportedPlatforms = setOf(PlatformType.JVM),
                )
            )
        )
    }
}