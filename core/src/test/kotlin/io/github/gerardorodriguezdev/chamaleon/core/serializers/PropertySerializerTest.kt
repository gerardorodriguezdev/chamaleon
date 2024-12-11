package io.github.gerardorodriguezdev.chamaleon.core.serializers

import io.github.gerardorodriguezdev.chamaleon.core.dtos.PlatformDto.PropertyDto
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.BooleanProperty
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.StringProperty
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class PropertySerializerTest {
    @Nested
    inner class Serialize {
        @Test
        fun `GIVEN propertyDto with empty name WHEN serialize THEN throws error`() {
            val propertyDto = PropertyDto(name = "", value = null)

            assertThrows<SerializationException> {
                Json.encodeToJsonElement(propertyDto)
            }
        }

        @Test
        fun `GIVEN propertyDto with empty value WHEN serialize THEN throws error`() {
            val propertyDto = PropertyDto(name = "Name", value = StringProperty(""))

            assertThrows<SerializationException> {
                Json.encodeToJsonElement(propertyDto)
            }
        }

        @Test
        fun `GIVEN valid value string WHEN serialize THEN returns json`() {
            //language=json
            val expectedJson = """{"name":"Name","value":"Value"}""".trimIndent()
            val propertyDto = PropertyDto(name = "Name", value = StringProperty("Value"))

            val actualJson = Json.encodeToString(propertyDto)

            assertEquals(expectedJson, actualJson)
        }

        @Test
        fun `GIVEN valid value boolean WHEN serialize THEN returns json`() {
            //language=json
            val expectedJson = """{"name":"Name","value":true}""".trimIndent()
            val propertyDto = PropertyDto(name = "Name", value = BooleanProperty(true))

            val actualJson = Json.encodeToString(propertyDto)

            assertEquals(expectedJson, actualJson)
        }

        @Test
        fun `GIVEN valid value null WHEN serialize THEN returns json`() {
            //language=json
            val expectedJson = """{"name":"Name"}""".trimIndent()
            val propertyDto = PropertyDto(name = "Name", value = null)

            val actualJson = Json.encodeToString(propertyDto)

            assertEquals(expectedJson, actualJson)
        }
    }

    @Nested
    inner class Deserialize {

        @Test
        fun `GIVEN json without name WHEN deserialize THEN throws error`() {
            val json =
                //language=json
                """
                {
                    "value": true
                }
                """.trimIndent()

            assertThrows<SerializationException> {
                Json.decodeFromString<PropertyDto>(json)
            }
        }

        @Test
        fun `GIVEN json without value WHEN deserialize THEN throws error`() {
            val json =
                //language=json
                """
                {
                    "name": "Something"
                }
                """.trimIndent()

            assertThrows<SerializationException> {
                Json.decodeFromString<PropertyDto>(json)
            }
        }

        @Test
        fun `GIVEN json with empty name WHEN deserialize THEN throws error`() {
            val json =
                //language=json
                """
                {
                    "name": "",
                    "value": true
                }
                """.trimIndent()

            assertThrows<SerializationException> {
                Json.decodeFromString<PropertyDto>(json)
            }
        }

        @Test
        fun `GIVEN json with empty value string WHEN deserialize THEN throws error`() {
            val json =
                //language=json
                """
                {
                    "name": "Something",
                    "value": ""
                }
                """.trimIndent()

            assertThrows<SerializationException> {
                Json.decodeFromString<PropertyDto>(json)
            }
        }

        @Test
        fun `GIVEN json with invalid type WHEN deserialize THEN throws error`() {
            val json =
                //language=json
                """
                {
                    "name": "Something",
                    "value": 20.0
                }
                """.trimIndent()

            assertThrows<SerializationException> {
                Json.decodeFromString<PropertyDto>(json)
            }
        }

        @Test
        fun `GIVEN valid value string WHEN deserialize THEN returns propertyDto`() {
            val expectedPropertyDto = PropertyDto(name = "Name", value = StringProperty("Value"))
            val json =
                //language=json
                """
                {
                    "name": "Name",
                    "value": "Value"
                }
                """.trimIndent()

            val actualPropertyDto = Json.decodeFromString<PropertyDto>(json)

            assertEquals(expectedPropertyDto, actualPropertyDto)
        }

        @Test
        fun `GIVEN valid value boolean WHEN deserialize THEN returns propertyDto`() {
            val expectedPropertyDto = PropertyDto(name = "Name", value = BooleanProperty(true))
            val json =
                //language=json
                """
                {
                    "name": "Name",
                    "value": true
                }
                """.trimIndent()

            val actualPropertyDto = Json.decodeFromString<PropertyDto>(json)

            assertEquals(expectedPropertyDto, actualPropertyDto)
        }

        @Test
        fun `GIVEN valid value null WHEN deserialize THEN returns propertyDto`() {
            val expectedPropertyDto = PropertyDto(name = "Name", value = null)
            val json =
                //language=json
                """
                {
                    "name": "Name",
                    "value": null
                }
                """.trimIndent()

            val actualPropertyDto = Json.decodeFromString<PropertyDto>(json)

            assertEquals(expectedPropertyDto, actualPropertyDto)
        }
    }
}