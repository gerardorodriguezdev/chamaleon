package io.github.gerardorodriguezdev.chamaleon.core.serializers

import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.NonEmptyString
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class NonEmptyStringSerializerTest {
    @Nested
    inner class Serialize {

        @Test
        fun `GIVEN non empty string WHEN serialize THEN returns json`() {
            //language=json
            val expectedJson = """{"value":"value"}""".trimIndent()
            val str = Str(value = NonEmptyString.unsafe("value"))

            val actualJson = Json.encodeToString(str)

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
                Json.decodeFromString<Str>(json)
            }
        }

        @Test
        fun `GIVEN valid string WHEN deserialize THEN returns nullable string`() {
            val expectedStr = Str(value = NonEmptyString.unsafe("value"))
            val json =
                //language=json
                """
                {
                    "value": "value"
                }
                """.trimIndent()

            val actualStr = Json.decodeFromString<Str>(json)

            assertEquals(expected = expectedStr, actual = actualStr)
        }
    }

    @Serializable
    private data class Str(
        val value: NonEmptyString
    )
}