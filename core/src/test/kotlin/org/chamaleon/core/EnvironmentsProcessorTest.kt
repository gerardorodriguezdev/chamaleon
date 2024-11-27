package org.chamaleon.core

import org.chamaleon.core.EnvironmentsProcessor.EnvironmentsProcessorException.*
import org.chamaleon.core.models.PropertyValue.StringProperty
import org.chamaleon.core.parsers.EnvironmentsParser.EnvironmentsParserResult
import org.chamaleon.core.parsers.SchemaParser.SchemaParserResult
import org.chamaleon.core.testing.TestData
import org.chamaleon.core.testing.fakes.FakeEnvironmentsParser
import org.chamaleon.core.testing.fakes.FakeSchemaParser
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class EnvironmentsProcessorTest {
    private val schemaParser = FakeSchemaParser()
    private val environmentsParser = FakeEnvironmentsParser()

    private val environmentsProcessor = EnvironmentsProcessor(
        schemaParser = schemaParser,
        environmentsParser = environmentsParser,
    )

    @Test
    fun `GIVEN schema parsing file not found WHEN environments THEN throws exception`() {
        schemaParser.schemaParserResult = SchemaParserResult.Failure.FileNotFound("")

        assertThrows<SchemaFileNotFound> {
            environmentsProcessor.environments()
        }
    }

    @Test
    fun `GIVEN schema parsing file is empty WHEN environments THEN throws exception`() {
        schemaParser.schemaParserResult = SchemaParserResult.Failure.FileIsEmpty("")

        assertThrows<SchemaFileIsEmpty> {
            environmentsProcessor.environments()
        }
    }

    @Test
    fun `GIVEN schema parsing serialization error WHEN environments THEN throws exception`() {
        schemaParser.schemaParserResult = SchemaParserResult.Failure.SerializationError(Exception())

        assertThrows<Exception> {
            environmentsProcessor.environments()
        }
    }

    @Test
    fun `GIVEN environments parsing serialization error WHEN environments THEN throws exception`() {
        environmentsParser.environmentsParserResult = EnvironmentsParserResult.Failure.SerializationError(Exception())

        assertThrows<Exception> {
            environmentsProcessor.environments()
        }
    }

    @Test
    fun `GIVEN environment is missing platform from schema WHEN environments THEN throws exception`() {
        environmentsParser.environmentsParserResult = EnvironmentsParserResult.Success(
            environments = setOf(
                TestData.validCompleteEnvironment.copy(
                    platforms = TestData.validCompleteEnvironment.platforms.drop(1).toSet()
                )
            )
        )

        assertThrows<PlatformsNotEqualToSchema> {
            environmentsProcessor.environments()
        }
    }

    @Test
    fun `GIVEN platform is missing property from schema WHEN environments THEN throws exception`() {
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
            environmentsProcessor.environments()
        }
    }

    @Test
    fun `GIVEN property has incorrect type from schema WHEN environments THEN throws exception`() {
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
            environmentsProcessor.environments()
        }
    }

    @Test
    fun `GIVEN valid schema and environments WHEN environments THEN returns environments`() {
        val expectedEnvironments = setOf(TestData.validCompleteEnvironment)

        val actualEnvironments = environmentsProcessor.environments()

        assertEquals(expectedEnvironments, actualEnvironments)
    }
}