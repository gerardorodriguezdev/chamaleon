package io.github.gerardorodriguezdev.chamaleon.core.serializers

import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

internal class PlatformTypeSerializerTest {

    @ParameterizedTest
    @MethodSource("serializationTestData")
    fun `GIVEN valid platform type WHEN serialize THEN returns string`(serializationTestData: SerializationTestData) {
        val actualPlatformTypeString = Json.encodeToString(serializationTestData.platformType)

        assertEquals(expected = serializationTestData.expectedPlatformTypeString, actual = actualPlatformTypeString)
    }

    @ParameterizedTest
    @MethodSource("invalidPlatformTypeStrings")
    fun `GIVEN invalid string WHEN deserialize THEN throws error`(string: String) {
        assertThrows<Exception> {
            Json.decodeFromString<PlatformType>(string)
        }
    }

    @ParameterizedTest
    @MethodSource("deserializationTestData")
    fun `GIVEN valid string WHEN deserialize THEN returns platform type`(deserializationTestData: DeserializationTestData) {
        val actualPlatformType = Json.decodeFromString<PlatformType>(deserializationTestData.platformTypeString)
        assertEquals(expected = deserializationTestData.expectedPlatformType, actual = actualPlatformType)
    }

    internal companion object {
        private fun String.wrapInQuotes(): String = "\"$this\""

        private fun PlatformType.wrapPlatformInQuotes(): String = serialName.wrapInQuotes()

        @JvmStatic
        fun serializationTestData(): List<SerializationTestData> =
            PlatformType.entries.map { platformType -> platformType.toSerializationTestData() }

        @JvmStatic
        fun deserializationTestData(): List<DeserializationTestData> =
            PlatformType.entries.map { platformType -> platformType.toDeserializationTestData() }

        @JvmStatic
        fun invalidPlatformTypeStrings(): List<String> =
            listOf(
                "",
                "\"",
                "\"\"",
                "JVM",
                "JVM".wrapInQuotes(),
                "jv".wrapInQuotes(),
            )

        private fun PlatformType.toSerializationTestData(): SerializationTestData =
            SerializationTestData(
                platformType = this,
                expectedPlatformTypeString = wrapPlatformInQuotes(),
            )

        private fun PlatformType.toDeserializationTestData(): DeserializationTestData =
            DeserializationTestData(
                expectedPlatformType = this,
                platformTypeString = this.wrapPlatformInQuotes(),
            )

        data class SerializationTestData(
            val platformType: PlatformType,
            val expectedPlatformTypeString: String,
        )

        data class DeserializationTestData(
            val platformTypeString: String,
            val expectedPlatformType: PlatformType,
        )
    }
}