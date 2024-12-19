package io.github.gerardorodriguezdev.chamaleon.core

import io.github.gerardorodriguezdev.chamaleon.core.DefaultEnvironmentsProcessor.DefaultEnvironmentsProcessorException.*
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.StringProperty
import io.github.gerardorodriguezdev.chamaleon.core.parsers.EnvironmentsParser.EnvironmentsParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser.PropertiesParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.SchemaParser.SchemaParserResult
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData
import io.github.gerardorodriguezdev.chamaleon.core.testing.fakes.FakeEnvironmentsParser
import io.github.gerardorodriguezdev.chamaleon.core.testing.fakes.FakePropertiesParser
import io.github.gerardorodriguezdev.chamaleon.core.testing.fakes.FakeSchemaParser
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals

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
    fun `GIVEN schema parsing file not found WHEN process THEN throws exception`() {
        schemaParser.schemaParserResult = SchemaParserResult.Failure.FileNotFound("")

        assertThrows<SchemaFileNotFound> {
            defaultEnvironmentsProcessor.process(directory)
        }
    }

    @Test
    fun `GIVEN schema parsing file is empty WHEN process THEN throws exception`() {
        schemaParser.schemaParserResult = SchemaParserResult.Failure.FileIsEmpty("")

        assertThrows<SchemaFileIsEmpty> {
            defaultEnvironmentsProcessor.process(directory)
        }
    }

    @Test
    fun `GIVEN schema parsing serialization error WHEN process THEN throws exception`() {
        schemaParser.schemaParserResult = SchemaParserResult.Failure.Serialization(Exception())

        assertThrows<Exception> {
            defaultEnvironmentsProcessor.process(directory)
        }
    }

    @Test
    fun `GIVEN environments parsing serialization error WHEN process THEN throws exception`() {
        environmentsParser.environmentsParserResult = EnvironmentsParserResult.Failure.Serialization(Exception())

        assertThrows<Exception> {
            defaultEnvironmentsProcessor.process(directory)
        }
    }

    @Test
    fun `GIVEN environment is missing platform from schema WHEN process THEN throws exception`() {
        environmentsParser.environmentsParserResult = EnvironmentsParserResult.Success(
            environments = setOf(
                TestData.validCompleteEnvironment.copy(
                    platforms = TestData.validCompleteEnvironment.platforms.drop(1).toSet()
                )
            )
        )

        assertThrows<PlatformsNotEqualToSchema> {
            defaultEnvironmentsProcessor.process(directory)
        }
    }

    @Test
    fun `GIVEN platform is missing property from schema WHEN process THEN throws exception`() {
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

        assertThrows<PropertiesNotEqualToSchema> {
            defaultEnvironmentsProcessor.process(directory)
        }
    }

    @Test
    fun `GIVEN property has incorrect type from schema WHEN process THEN throws exception`() {
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

        assertThrows<PropertyTypeNotMatchSchema> {
            defaultEnvironmentsProcessor.process(directory)
        }
    }

    @Test
    fun `GIVEN invalid properties file WHEN process THEN throws exception`() {
        propertiesParser.propertiesParserResult = PropertiesParserResult.Failure(Exception())

        assertThrows<Exception> {
            defaultEnvironmentsProcessor.process(directory)
        }
    }

    @Test
    fun `GIVEN properties file parsing error WHEN process THEN throws exception`() {
        propertiesParser.propertiesParserResult = PropertiesParserResult.Failure(Exception())

        assertThrows<Exception> {
            defaultEnvironmentsProcessor.process(directory)
        }
    }

    @Test
    fun `GIVEN valid schema and environments with selected environment WHEN process THEN returns environments`() {
        val expectedEnvironmentsProcessorResult = EnvironmentsProcessor.EnvironmentsProcessorResult(
            selectedEnvironmentName = TestData.ENVIRONMENT_NAME,
            environments = setOf(TestData.validCompleteEnvironment),
        )

        val actualEnvironmentsProcessorResult = defaultEnvironmentsProcessor.process(directory)

        assertEquals(actualEnvironmentsProcessorResult, expectedEnvironmentsProcessorResult)
    }
}