package io.github.gerardorodriguezdev.chamaleon.core.serializers

import io.github.gerardorodriguezdev.chamaleon.core.safeModels.KeyProvider
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore.Companion.toNonEmptyKeySetStore
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class NonEmptyKeyStoreSerializerTest {

    @Nested
    inner class Serialize {

        @Test
        fun `GIVEN non empty key set store WHEN serialize THEN returns json`() {
            val actualJson = Json.encodeToString(nonEmptyKeySetStore)

            assertEquals(expected = validJson, actual = actualJson)
        }
    }

    @Nested
    inner class Deserialize {

        @Test
        fun `GIVEN json with empty key set store WHEN deserialize THEN throws`() {
            assertThrows<SerializationException> {
                Json.decodeFromString<NonEmptyKeySetStore<String, StringKeyProvider>>(invalidJson)
            }
        }

        @Test
        fun `GIVEN valid non empty key set store WHEN deserialize THEN returns non empty key set store`() {
            val actualNonEmptyKeySetStore =
                Json.decodeFromString<NonEmptyKeySetStore<String, StringKeyProvider>>(validJson)

            assertEquals(expected = nonEmptyKeySetStore, actual = actualNonEmptyKeySetStore)
        }
    }

    private companion object {
        val nonEmptyKeySetStore = setOf(StringKeyProvider("key1"), StringKeyProvider("key2")).toNonEmptyKeySetStore()
        val invalidJson =
            //language=json
            """
              []
            """.trimIndent()
        val validJson =
            //language=json
            """[{"key":"key1"},{"key":"key2"}]""".trimIndent()
    }

    @Serializable
    private data class StringKeyProvider(override val key: String) : KeyProvider<String>
}