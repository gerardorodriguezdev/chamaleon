package io.github.gerardorodriguezdev.chamaleon.core.serializers

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class NonEmptyNullableStringSerializerTest {
    @Nested
    inner class Serialize {
        @Test
        fun `GIVEN empty string WHEN serialize THEN throws error`() {
            val nullableString = NullableString(value = "")

            assertThrows<SerializationException> {
                Json.encodeToJsonElement(nullableString)
            }
        }

        @Test
        fun `GIVEN non empty string WHEN serialize THEN returns json`() {
            //language=json
            val expectedJson = """{"value":"value"}""".trimIndent()
            val nullableString = NullableString(value = "value")

            val actualJson = Json.encodeToString(nullableString)

            assertEquals(expected = expectedJson, actual = actualJson)
        }
    }

    @Nested
    inner class Deserialize {

        @Test
        fun `GIVEN json with empty string WHEN deserialize THEN throws error`() {
            val json =
                //language=json
                """
                {
                    "value": ""
                }
                """.trimIndent()

            assertThrows<SerializationException> {
                Json.decodeFromString<NullableString>(json)
            }
        }

        @Test
        fun `GIVEN valid null WHEN deserialize THEN returns nullable string`() {
            val expectedNullableString = NullableString(null)
            val json =
                //language=json
                """
                {
                    "value": null
                }
                """.trimIndent()

            val actualNullableString = Json.decodeFromString<NullableString>(json)

            assertEquals(expected = expectedNullableString, actual = actualNullableString)
        }

        @Test
        fun `GIVEN valid string WHEN deserialize THEN returns nullable string`() {
            val expectedNullableString = NullableString(value = "value")
            val json =
                //language=json
                """
                {
                    "value": "value"
                }
                """.trimIndent()

            val actualNullableString = Json.decodeFromString<NullableString>(json)

            assertEquals(expected = expectedNullableString, actual = actualNullableString)
        }
    }

    @Serializable
    private data class NullableString(
        @Serializable(with = NonEmptyNullableStringSerializer::class)
        val value: String?
    )
}