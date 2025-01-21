package io.github.gerardorodriguezdev.chamaleon.core.entities

import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PlatformTest {
    private val platform = Platform(
        platformType = PlatformType.JVM,
        properties = TestData.validCompleteProperties,
    )

    @Test
    fun `WHEN propertyStringValue THEN returns value`() {
        val actual = platform.propertyStringValue(TestData.DOMAIN_PROPERTY_NAME)
        assertEquals(expected = TestData.DOMAIN, actual = actual)
    }

    @Test
    fun `WHEN propertyStringValueOrNull THEN returns null`() {
        val actual = platform.propertyStringValueOrNull(TestData.HOST_PROPERTY_NAME)
        assertEquals(expected = null, actual = actual)
    }

    @Test
    fun `WHEN propertyBooleanValue THEN returns value`() {
        val actual = platform.propertyBooleanValue(TestData.IS_PRODUCTION_PROPERTY_NAME)
        assertEquals(expected = true, actual = actual)
    }

    @Test
    fun `WHEN propertyBooleanValueOrNull THEN returns null`() {
        val actual = platform.propertyBooleanValueOrNull(TestData.IS_DEBUG_PROPERTY_NAME)
        assertEquals(expected = null, actual = actual)
    }

}