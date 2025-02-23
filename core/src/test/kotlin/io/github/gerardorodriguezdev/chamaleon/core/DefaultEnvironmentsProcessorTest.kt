package io.github.gerardorodriguezdev.chamaleon.core

import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform
import io.github.gerardorodriguezdev.chamaleon.core.models.PlatformType.ANDROID
import io.github.gerardorodriguezdev.chamaleon.core.models.PlatformType.JVM
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.StringProperty
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsParserResult
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsProcessorResult
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsProcessorResult.Failure.*
import io.github.gerardorodriguezdev.chamaleon.core.results.PropertiesParserResult
import io.github.gerardorodriguezdev.chamaleon.core.results.SchemaParserResult
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData.LOCAL_ENVIRONMENT_NAME
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData.domainProperty
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData.hostProperty
import io.github.gerardorodriguezdev.chamaleon.core.testing.fakes.FakeEnvironmentsParser
import io.github.gerardorodriguezdev.chamaleon.core.testing.fakes.FakePropertiesParser
import io.github.gerardorodriguezdev.chamaleon.core.testing.fakes.FakeSchemaParser
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DefaultEnvironmentsProcessorTest {
    @TempDir
    lateinit var directory: File
    private val environmentsDirectory by lazy { createEnvironmentsDirectory() }

    private val schemaParser = FakeSchemaParser()
    private val environmentsParser = FakeEnvironmentsParser()
    private val propertiesParser = FakePropertiesParser()

    private val defaultEnvironmentsProcessor = DefaultEnvironmentsProcessor(
        schemaParser = schemaParser,
        environmentsParser = environmentsParser,
        propertiesParser = propertiesParser,
    )

    @Nested
    inner class Process {
        @Test
        fun `GIVEN environments directory not found WHEN process THEN returns failure`() = runTest {
            environmentsDirectory.delete()

            assertIs<EnvironmentsDirectoryNotFound>(
                defaultEnvironmentsProcessor.process(environmentsDirectory)
            )
        }

        @Test
        fun `GIVEN schema parsing file not found WHEN process THEN returns failure`() = runTest {
            schemaParser.schemaParserResult = SchemaParserResult.Failure.FileNotFound("")

            assertIs<SchemaParsingError>(
                defaultEnvironmentsProcessor.process(environmentsDirectory)
            )
        }

        @Test
        fun `GIVEN environments parsing serialization error WHEN process THEN returns failure`() = runTest {
            environmentsParser.environmentsParserResult =
                EnvironmentsParserResult.Failure.Serialization("", Exception())

            assertIs<EnvironmentsParsingError>(
                defaultEnvironmentsProcessor.process(environmentsDirectory)
            )
        }

        @Test
        fun `GIVEN invalid properties file WHEN process THEN returns failure`() = runTest {
            propertiesParser.propertiesParserResult = PropertiesParserResult.Failure.Serialization("", Exception())

            assertIs<PropertiesParsingError>(
                defaultEnvironmentsProcessor.process(environmentsDirectory)
            )
        }

        @Test
        fun `GIVEN environment is missing platform from schema WHEN process THEN returns failure`() = runTest {
            environmentsParser.environmentsParserResult = EnvironmentsParserResult.Success(
                environments = setOf(
                    TestData.environment.copy(
                        platforms = TestData.environment.platforms.drop(1).toSet()
                    )
                )
            )

            assertIs<EnvironmentMissingPlatforms>(
                defaultEnvironmentsProcessor.process(environmentsDirectory)
            )
        }

        @Test
        fun `GIVEN platform is missing property from schema WHEN process THEN returns failure`() = runTest {
            environmentsParser.environmentsParserResult = EnvironmentsParserResult.Success(
                environments = setOf(
                    TestData.environment.copy(
                        platforms = TestData.environment.platforms.map { platformDto ->
                            platformDto.copy(
                                properties = platformDto.properties.drop(1).toSet()
                            )
                        }.toSet()
                    )
                )
            )

            assertIs<PropertyNotEqualToPropertyDefinition>(
                defaultEnvironmentsProcessor.process(environmentsDirectory)
            )
        }

        @Test
        fun `GIVEN platform with forbidden property definition WHEN process THEN returns environments`() =
            runTest {
                schemaParser.schemaParserResult = SchemaParserResult.Success(TestData.schemaWithRestrictedPlatformTypes)
                environmentsParser.environmentsParserResult = EnvironmentsParserResult.Success(
                    setOf(
                        Environment(
                            name = LOCAL_ENVIRONMENT_NAME,
                            platforms = setOf(
                                Platform(
                                    platformType = ANDROID,
                                    properties = setOf(
                                        hostProperty,
                                        domainProperty,
                                    )
                                ),
                                Platform(
                                    platformType = JVM,
                                    properties = setOf(
                                        hostProperty,
                                        domainProperty
                                    )
                                ),
                            )
                        )
                    )
                )

                assertIs<PropertyNotEqualToPropertyDefinition>(
                    defaultEnvironmentsProcessor.process(environmentsDirectory)
                )
            }

        @Test
        fun `GIVEN property has incorrect type from schema WHEN process THEN returns failure`() = runTest {
            environmentsParser.environmentsParserResult = EnvironmentsParserResult.Success(
                environments = setOf(
                    TestData.environment.copy(
                        platforms = TestData.environment.platforms.map { platformDto ->
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

            assertIs<PropertyTypeNotEqualToPropertyDefinition>(
                defaultEnvironmentsProcessor.process(environmentsDirectory)
            )
        }

        @Test
        fun `GIVEN non null property definition is null WHEN process THEN returns failure`() = runTest {
            environmentsParser.environmentsParserResult = EnvironmentsParserResult.Success(
                environments = setOf(
                    TestData.environment.copy(
                        platforms = TestData.environment.platforms.map { platformDto ->
                            platformDto.copy(
                                properties = platformDto.properties.map { propertyDto ->
                                    if (propertyDto.name == "DOMAIN") {
                                        propertyDto.copy(value = null)
                                    } else {
                                        propertyDto
                                    }
                                }.toSet()
                            )
                        }.toSet()
                    )
                )
            )

            assertIs<NullPropertyNotNullable>(
                defaultEnvironmentsProcessor.process(environmentsDirectory)
            )
        }

        @Test
        fun `GIVEN selected environment not in environments WHEN process THEN returns failure`() = runTest {
            propertiesParser.propertiesParserResult =
                PropertiesParserResult.Success(selectedEnvironmentName = "NonExisting")

            assertIs<SelectedEnvironmentNotFound>(
                defaultEnvironmentsProcessor.process(environmentsDirectory)
            )
        }

        @Test
        fun `GIVEN valid schema and environments with omitted property WHEN process THEN returns environments`() =
            runTest {
                schemaParser.schemaParserResult = SchemaParserResult.Success(TestData.schemaWithRestrictedPlatformTypes)
                val validEnvironments = setOf(TestData.environmentWithRestrictedPlatform)
                environmentsParser.environmentsParserResult = EnvironmentsParserResult.Success(validEnvironments)

                val expectedEnvironmentsProcessorResult = EnvironmentsProcessorResult.Success(
                    environmentsDirectoryPath = environmentsDirectory.path,
                    selectedEnvironmentName = LOCAL_ENVIRONMENT_NAME,
                    environments = validEnvironments,
                    schema = TestData.schemaWithRestrictedPlatformTypes,
                )

                val environmentsProcessorResult = defaultEnvironmentsProcessor.process(environmentsDirectory)

                assertEquals(expected = expectedEnvironmentsProcessorResult, actual = environmentsProcessorResult)
            }

        @Test
        fun `GIVEN valid schema and environments with selected environment WHEN process THEN returns environments`() =
            runTest {
                val expectedEnvironmentsProcessorResult = EnvironmentsProcessorResult.Success(
                    environmentsDirectoryPath = environmentsDirectory.path,
                    selectedEnvironmentName = LOCAL_ENVIRONMENT_NAME,
                    environments = setOf(TestData.environment),
                    schema = TestData.schema,
                )

                val environmentsProcessorResult = defaultEnvironmentsProcessor.process(environmentsDirectory)

                assertEquals(expected = expectedEnvironmentsProcessorResult, actual = environmentsProcessorResult)
            }
    }

    @Nested
    inner class ProcessRecursively {
        @Test
        fun `GIVEN valid environments WHEN processRecursively THEN returns results list`() = runTest {
            val environmentsDirectory = createEnvironmentsDirectory()

            val environmentsProcessorResults = defaultEnvironmentsProcessor.processRecursively(directory)

            assertEquals(
                expected = listOf(
                    EnvironmentsProcessorResult.Success(
                        environmentsDirectoryPath = environmentsDirectory.path,
                        selectedEnvironmentName = LOCAL_ENVIRONMENT_NAME,
                        environments = setOf(
                            TestData.environment
                        ),
                        schema = TestData.schema,
                    )
                ),
                actual = environmentsProcessorResults,
            )
        }
    }

    @Test
    fun `GIVEN environmentName WHEN environmentFileName THEN returns environment name`() {
        val environmentFileName = EnvironmentsProcessor.environmentFileName(LOCAL_ENVIRONMENT_NAME)
        assertEquals(expected = "local.environment.chamaleon.json", actual = environmentFileName)
    }

    private fun createEnvironmentsDirectory(): File {
        val environmentsDirectory = File(directory, EnvironmentsProcessor.ENVIRONMENTS_DIRECTORY_NAME)
        environmentsDirectory.mkdir()
        return environmentsDirectory
    }
}