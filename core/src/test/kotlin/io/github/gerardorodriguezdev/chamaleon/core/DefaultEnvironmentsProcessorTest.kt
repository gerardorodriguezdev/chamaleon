package io.github.gerardorodriguezdev.chamaleon.core

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.EnvironmentsProcessorResult.Failure.*
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyValue.StringProperty
import io.github.gerardorodriguezdev.chamaleon.core.parsers.EnvironmentsParser.EnvironmentsParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser.PropertiesParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.SchemaParser.SchemaParserResult
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData
import io.github.gerardorodriguezdev.chamaleon.core.testing.fakes.FakeEnvironmentsParser
import io.github.gerardorodriguezdev.chamaleon.core.testing.fakes.FakePropertiesParser
import io.github.gerardorodriguezdev.chamaleon.core.testing.fakes.FakeSchemaParser
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DefaultEnvironmentsProcessorTest {
    @TempDir
    lateinit var directory: File

    private val schemaParser = FakeSchemaParser()
    private val environmentsParser = FakeEnvironmentsParser()
    private val propertiesParser = FakePropertiesParser()

    private val defaultEnvironmentsProcessor = DefaultEnvironmentsProcessor(
        schemaParser = schemaParser,
        environmentsParser = environmentsParser,
        propertiesParser = propertiesParser,
    )

    @Test
    fun `GIVEN schema parsing file not found WHEN process THEN returns failure`() = runTest {
        schemaParser.schemaParserResult = SchemaParserResult.Failure.FileNotFound("")

        assertIs<SchemaFileNotFound>(
            defaultEnvironmentsProcessor.process(directory)
        )
    }

    @Test
    fun `GIVEN schema parsing file is empty WHEN process THEN returns failure`() = runTest {
        schemaParser.schemaParserResult = SchemaParserResult.Failure.FileIsEmpty("")

        assertIs<SchemaFileIsEmpty>(
            defaultEnvironmentsProcessor.process(directory)
        )
    }

    @Test
    fun `GIVEN schema parsing serialization error WHEN process THEN returns failure`() = runTest {
        schemaParser.schemaParserResult = SchemaParserResult.Failure.Serialization(Exception())

        assertIs<SchemaSerialization>(
            defaultEnvironmentsProcessor.process(directory)
        )
    }

    @Test
    fun `GIVEN environments parsing serialization error WHEN process THEN returns failure`() = runTest {
        environmentsParser.environmentsParserResult = EnvironmentsParserResult.Failure.Serialization(Exception())

        assertIs<EnvironmentsSerialization>(
            defaultEnvironmentsProcessor.process(directory)
        )
    }

    @Test
    fun `GIVEN environment is missing platform from schema WHEN process THEN returns failure`() = runTest {
        environmentsParser.environmentsParserResult = EnvironmentsParserResult.Success(
            environments = setOf(
                TestData.validCompleteEnvironment.copy(
                    platforms = TestData.validCompleteEnvironment.platforms.drop(1).toSet()
                )
            )
        )

        assertIs<PlatformsNotEqualToSchema>(
            defaultEnvironmentsProcessor.process(directory)
        )
    }

    @Test
    fun `GIVEN platform is missing property from schema WHEN process THEN returns failure`() = runTest {
        environmentsParser.environmentsParserResult = EnvironmentsParserResult.Success(
            environments = setOf(
                TestData.validCompleteEnvironment.copy(
                    platforms = TestData.validCompleteEnvironment.platforms.map { platformDto ->
                        platformDto.copy(
                            properties = platformDto.properties.drop(1).toSet()
                        )
                    }.toSet()
                )
            )
        )

        assertIs<PropertiesNotEqualToSchema>(
            defaultEnvironmentsProcessor.process(directory)
        )
    }

    @Test
    fun `GIVEN property has incorrect type from schema WHEN process THEN returns failure`() = runTest {
        environmentsParser.environmentsParserResult = EnvironmentsParserResult.Success(
            environments = setOf(
                TestData.validCompleteEnvironment.copy(
                    platforms = TestData.validCompleteEnvironment.platforms.map { platformDto ->
                        platformDto.copy(
                            properties = platformDto.properties.map { propertyDto ->
                                propertyDto.copy(
                                    value = StringProperty("Value"),
                                )
                            }.toSet()
                        )
                    }.toSet()
                )
            )
        )

        assertIs<PropertyTypeNotMatchSchema>(
            defaultEnvironmentsProcessor.process(directory)
        )
    }

    @Test
    fun `GIVEN invalid properties file WHEN process THEN returns failure`() = runTest {
        propertiesParser.propertiesParserResult = PropertiesParserResult.Failure(Exception())

        assertIs<PropertiesSerialization>(
            defaultEnvironmentsProcessor.process(directory)
        )
    }

    @Test
    fun `GIVEN properties file parsing error WHEN process THEN returns failure`() = runTest {
        propertiesParser.propertiesParserResult = PropertiesParserResult.Failure(Exception())

        assertIs<PropertiesSerialization>(
            defaultEnvironmentsProcessor.process(directory)
        )
    }

    @Test
    fun `GIVEN valid schema and environments with selected environment WHEN process THEN returns environments`() =
        runTest {
            val expectedEnvironmentsProcessorResult = EnvironmentsProcessor.EnvironmentsProcessorResult.Success(
                selectedEnvironmentName = TestData.ENVIRONMENT_NAME,
                environments = setOf(TestData.validCompleteEnvironment),
            )

            val environmentsProcessorResult = defaultEnvironmentsProcessor.process(directory)

            assertEquals(environmentsProcessorResult, expectedEnvironmentsProcessorResult)
        }

    @Test
    fun `GIVEN valid environments WHEN processRecursively THEN returns results list`() = runTest {
        createEnvironmentsDirectory()

        val environmentsProcessorResults = defaultEnvironmentsProcessor.processRecursively(directory)

        assertEquals(
            listOf(
                EnvironmentsProcessor.EnvironmentsProcessorResult.Success(
                    selectedEnvironmentName = TestData.ENVIRONMENT_NAME,
                    environments = setOf(
                        TestData.validCompleteEnvironment
                    )
                )
            ),
            environmentsProcessorResults,
        )
    }

    @Test
    fun `GIVEN invalid environments WHEN processRecursively THEN returns results list`() = runTest {
        val expectedException = Exception()
        createEnvironmentsDirectory()
        propertiesParser.propertiesParserResult = PropertiesParserResult.Failure(expectedException)

        val environmentsProcessorResults = defaultEnvironmentsProcessor.processRecursively(directory)

        assertEquals(
            listOf(PropertiesSerialization(expectedException)),
            environmentsProcessorResults,
        )
    }

    @Test
    fun `WHEN updateSelectedEnvironmentResult THEN returns true`() {
        propertiesParser.updateSelectedEnvironmentResult = true

        val updateSelectedEnvironmentResult =
            defaultEnvironmentsProcessor.updateSelectedEnvironment(
                environmentsDirectory = directory,
                newSelectedEnvironment = TestData.ENVIRONMENT_NAME,
            )

        assertTrue { updateSelectedEnvironmentResult }
    }

    private fun createEnvironmentsDirectory() {
        val environmentsDirectory = File(directory, EnvironmentsProcessor.ENVIRONMENTS_DIRECTORY_NAME)
        environmentsDirectory.mkdir()
    }
}