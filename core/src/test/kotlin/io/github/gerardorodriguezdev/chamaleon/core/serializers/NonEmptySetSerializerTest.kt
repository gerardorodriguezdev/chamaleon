package io.github.gerardorodriguezdev.chamaleon.core.serializers

import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptySet
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptySet.Companion.toUnsafeNonEmptySet
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class NonEmptySetSerializerTest {

    @Nested
    inner class Serialize {
        @Test
        fun `GIVEN non empty set WHEN serialize THEN returns json`() {
            //language=json
            val expectedJson = """{"set":[1,2,3]}""".trimIndent()
            val nullableString = IntSet(setOf(1, 2, 3).toUnsafeNonEmptySet())

            val actualJson = Json.encodeToString(nullableString)

            assertEquals(expected = expectedJson, actual = actualJson)
        }
    }

    @Nested
    inner class Deserialize {

        @Test
        fun `GIVEN json with empty set WHEN deserialize THEN throws`() {
            val json =
                //language=json
                """
                {
                    "set": []
                }
                """.trimIndent()

            assertThrows<SerializationException> {
                Json.decodeFromString<IntSet>(json)
            }
        }

        @Test
        fun `GIVEN valid non empty set WHEN deserialize THEN returns set`() {
            val expectedSet = IntSet(setOf(1, 2, 3).toUnsafeNonEmptySet())
            val json =
                //language=json
                """
                {
                    "set": [1,2,3]
                }
                """.trimIndent()

            val actualSet = Json.decodeFromString<IntSet>(json)

            assertEquals(expected = expectedSet.set.value, actual = actualSet.set.value)
        }
    }

    @Serializable
    private data class IntSet(
        val set: NonEmptySet<Int>,
    )
}