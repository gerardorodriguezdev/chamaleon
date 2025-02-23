package io.github.gerardorodriguezdev.chamaleon.core.models

import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PlatformTest {
    @Test
    fun `WHEN propertyStringValue THEN returns value`() {
        val actual = TestData.jvmPlatform.propertyStringValue(TestData.DOMAIN_PROPERTY_NAME)
        assertEquals(expected = TestData.DOMAIN, actual = actual)
    }

    @Test
    fun `WHEN propertyStringValueOrNull THEN returns null`() {
        val actual = TestData.jvmPlatform.propertyStringValueOrNull(TestData.HOST_PROPERTY_NAME)
        assertEquals(expected = null, actual = actual)
    }

    @Test
    fun `WHEN propertyBooleanValue THEN returns value`() {
        val actual = TestData.jvmPlatform.propertyBooleanValue(TestData.IS_PRODUCTION_PROPERTY_NAME)
        assertEquals(expected = true, actual = actual)
    }

    @Test
    fun `WHEN propertyBooleanValueOrNull THEN returns null`() {
        val actual = TestData.jvmPlatform.propertyBooleanValueOrNull(TestData.IS_DEBUG_PROPERTY_NAME)
        assertEquals(expected = null, actual = actual)
    }
}