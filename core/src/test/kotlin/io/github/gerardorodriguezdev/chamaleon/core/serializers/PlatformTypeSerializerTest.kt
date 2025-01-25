package io.github.gerardorodriguezdev.chamaleon.core.serializers

import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

internal class PlatformTypeSerializerTest {

    @ParameterizedTest
    @MethodSource("serializationTestCases")
    fun `GIVEN valid platform type WHEN serialize THEN returns string`(serializationTestCase: SerializationTestCase) {
        val actualPlatformTypeString = Json.encodeToString(serializationTestCase.platformType)

        assertEquals(expected = serializationTestCase.expectedPlatformTypeString, actual = actualPlatformTypeString)
    }

    @ParameterizedTest
    @MethodSource("invalidPlatformTypeStrings")
    fun `GIVEN invalid string WHEN deserialize THEN throws error`(string: String) {
        assertThrows<Exception> {
            Json.decodeFromString<PlatformType>(string)
        }
    }

    @ParameterizedTest
    @MethodSource("deserializationTestCases")
    fun `GIVEN valid string WHEN deserialize THEN returns platform type`(
        deserializationTestCase: DeserializationTestCase
    ) {
        val actualPlatformType = Json.decodeFromString<PlatformType>(deserializationTestCase.platformTypeString)
        assertEquals(expected = deserializationTestCase.expectedPlatformType, actual = actualPlatformType)
    }

    internal companion object {
        private fun String.wrapInQuotes(): String = "\"$this\""

        private fun PlatformType.wrapPlatformInQuotes(): String = serialName.wrapInQuotes()

        @JvmStatic
        fun serializationTestCases(): List<SerializationTestCase> =
            PlatformType.entries.map { platformType -> platformType.toSerializationTestData() }

        @JvmStatic
        fun deserializationTestCases(): List<DeserializationTestCase> =
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

        private fun PlatformType.toSerializationTestData(): SerializationTestCase =
            SerializationTestCase(
                platformType = this,
                expectedPlatformTypeString = wrapPlatformInQuotes(),
            )

        private fun PlatformType.toDeserializationTestData(): DeserializationTestCase =
            DeserializationTestCase(
                expectedPlatformType = this,
                platformTypeString = this.wrapPlatformInQuotes(),
            )

        data class SerializationTestCase(
            val platformType: PlatformType,
            val expectedPlatformTypeString: String,
        )

        data class DeserializationTestCase(
            val platformTypeString: String,
            val expectedPlatformType: PlatformType,
        )
    }
}