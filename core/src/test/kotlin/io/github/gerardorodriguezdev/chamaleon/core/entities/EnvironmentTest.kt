package io.github.gerardorodriguezdev.chamaleon.core.entities

import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EnvironmentTest {

    @Test
    fun `WHEN wasmPlatform THEN returns platform`() {
        val actual = TestData.environment.wasmPlatform()
        assertEquals(expected = TestData.wasmPlatform, actual = actual)
    }

    @Test
    fun `GIVEN no platform WHEN wasmPlatformOrNull THEN returns null`() {
        val actual = TestData.environment.copy(platforms = emptySet()).wasmPlatformOrNull()
        assertEquals(expected = null, actual = actual)
    }

    @Test
    fun `WHEN androidPlatform THEN returns platform`() {
        val actual = TestData.environment.androidPlatform()
        assertEquals(expected = TestData.androidPlatform, actual = actual)
    }

    @Test
    fun `GIVEN no platform WHEN androidPlatformOrNull THEN returns null`() {
        val actual = TestData.environment.copy(platforms = emptySet()).androidPlatformOrNull()
        assertEquals(expected = null, actual = actual)
    }

    @Test
    fun `WHEN iosPlatform THEN returns value`() {
        val actual = TestData.environment.iosPlatform()
        assertEquals(expected = TestData.iosPlatform, actual = actual)
    }

    @Test
    fun `GIVEN no platform WHEN iosPlatformOrNull THEN returns null`() {
        val actual = TestData.environment.copy(platforms = emptySet()).iosPlatformOrNull()
        assertEquals(expected = null, actual = actual)
    }

    @Test
    fun `WHEN jvmPlatform THEN returns value`() {
        val actual = TestData.environment.jvmPlatform()
        assertEquals(expected = TestData.jvmPlatform, actual = actual)
    }

    @Test
    fun `GIVEN no platform WHEN jvmPlatformOrNull THEN returns null`() {
        val actual = TestData.environment.copy(platforms = emptySet()).jvmPlatformOrNull()
        assertEquals(expected = null, actual = actual)
    }

}