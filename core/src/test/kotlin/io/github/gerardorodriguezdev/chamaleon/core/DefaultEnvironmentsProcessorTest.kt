package io.github.gerardorodriguezdev.chamaleon.core

import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Platform
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType.ANDROID
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType.JVM
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyValue.StringProperty
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.EnvironmentsProcessorResult
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.EnvironmentsProcessorResult.Failure.*
import io.github.gerardorodriguezdev.chamaleon.core.parsers.EnvironmentsParser.EnvironmentsParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser.PropertiesParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.SchemaParser.SchemaParserResult
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData.LOCAL_ENVIRONMENT_NAME
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData.PRODUCTION_ENVIRONMENT_NAME
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData.domainProperty
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData.hostProperty
import io.github.gerardorodriguezdev.chamaleon.core.testing.fakes.FakeEnvironmentsParser
import io.github.gerardorodriguezdev.chamaleon.core.testing.fakes.FakePropertiesParser
import io.github.gerardorodriguezdev.chamaleon.core.testing.fakes.FakeSchemaParser
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

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

            assertIs<SchemaFileNotFound>(
                defaultEnvironmentsProcessor.process(environmentsDirectory)
            )
        }

        @Test
        fun `GIVEN schema parsing file is empty WHEN process THEN returns failure`() = runTest {
            schemaParser.schemaParserResult = SchemaParserResult.Failure.FileIsEmpty("")

            assertIs<SchemaFileIsEmpty>(
                defaultEnvironmentsProcessor.process(environmentsDirectory)
            )
        }

        @Test
        fun `GIVEN schema parsing serialization error WHEN process THEN returns failure`() = runTest {
            schemaParser.schemaParserResult = SchemaParserResult.Failure.Serialization(Exception())

            assertIs<SchemaSerialization>(
                defaultEnvironmentsProcessor.process(environmentsDirectory)
            )
        }

        @Test
        fun `GIVEN schema parsing with empty supported platforms WHEN process THEN returns failure`() =
            runTest {
                schemaParser.schemaParserResult = SchemaParserResult.Failure.EmptySupportedPlatforms(
                    path = "",
                )

                assertIs<SchemaEmptySupportedPlatforms>(
                    defaultEnvironmentsProcessor.process(environmentsDirectory)
                )
            }

        @Test
        fun `GIVEN schema parsing with empty property definitions WHEN process THEN returns failure`() =
            runTest {
                schemaParser.schemaParserResult = SchemaParserResult.Failure.EmptyPropertyDefinitions(
                    path = "",
                )

                assertIs<SchemaEmptyPropertyDefinitions>(
                    defaultEnvironmentsProcessor.process(environmentsDirectory)
                )
            }

        @Test
        fun `GIVEN schema parsing with invalid property definitions WHEN process THEN returns failure`() =
            runTest {
                schemaParser.schemaParserResult = SchemaParserResult.Failure.InvalidPropertyDefinition(
                    path = "",
                )

                assertIs<SchemaInvalidPropertyDefinitions>(
                    defaultEnvironmentsProcessor.process(environmentsDirectory)
                )
            }

        @Test
        fun `GIVEN schema parsing with duplicated property definitions WHEN process THEN returns failure`() =
            runTest {
                schemaParser.schemaParserResult = SchemaParserResult.Failure.DuplicatedPropertyDefinition(
                    path = "",
                )

                assertIs<SchemaDuplicatedPropertyDefinition>(
                    defaultEnvironmentsProcessor.process(environmentsDirectory)
                )
            }

        @Test
        fun `GIVEN environments parsing serialization error WHEN process THEN returns failure`() = runTest {
            environmentsParser.environmentsParserResult = EnvironmentsParserResult.Failure.Serialization(Exception())

            assertIs<EnvironmentsSerialization>(
                defaultEnvironmentsProcessor.process(environmentsDirectory)
            )
        }

        @Test
        fun `GIVEN invalid properties file WHEN process THEN returns failure`() = runTest {
            propertiesParser.propertiesParserResult = PropertiesParserResult.Failure(Exception())

            assertIs<PropertiesSerialization>(
                defaultEnvironmentsProcessor.process(environmentsDirectory)
            )
        }

        @Test
        fun `GIVEN properties file parsing error WHEN process THEN returns failure`() = runTest {
            propertiesParser.propertiesParserResult = PropertiesParserResult.Failure(Exception())

            assertIs<PropertiesSerialization>(
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

            assertIs<PlatformsNotEqualToSchema>(
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

            assertIs<PropertiesNotEqualToSchema>(
                defaultEnvironmentsProcessor.process(environmentsDirectory)
            )
        }

        @Test
        fun `GIVEN platform with forbidden property definition WHEN process THEN returns environments`() =
            runTest {
                schemaParser.schemaParserResult = SchemaParserResult.Success(TestData.schemaWithRestrictedPlatform)
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

                assertIs<PropertiesNotEqualToSchema>(
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

            assertIs<PropertyTypeNotMatchSchema>(
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

            assertIs<NullPropertyNotNullableOnSchema>(
                defaultEnvironmentsProcessor.process(environmentsDirectory)
            )
        }

        @Test
        fun `GIVEN selected environment not in environments WHEN process THEN returns failure`() = runTest {
            propertiesParser.propertiesParserResult =
                PropertiesParserResult.Success(selectedEnvironmentName = "NonExisting")

            assertIs<SelectedEnvironmentInvalid>(
                defaultEnvironmentsProcessor.process(environmentsDirectory)
            )
        }

        @Test
        fun `GIVEN valid schema and environments with omitted property WHEN process THEN returns environments`() =
            runTest {
                schemaParser.schemaParserResult = SchemaParserResult.Success(TestData.schemaWithRestrictedPlatform)
                val validEnvironments = setOf(TestData.environmentWithRestrictedPlatform)
                environmentsParser.environmentsParserResult = EnvironmentsParserResult.Success(validEnvironments)

                val expectedEnvironmentsProcessorResult = EnvironmentsProcessorResult.Success(
                    environmentsDirectoryPath = environmentsDirectory.absolutePath,
                    selectedEnvironmentName = LOCAL_ENVIRONMENT_NAME,
                    environments = validEnvironments,
                )

                val environmentsProcessorResult = defaultEnvironmentsProcessor.process(environmentsDirectory)

                assertEquals(expected = expectedEnvironmentsProcessorResult, actual = environmentsProcessorResult)
            }

        @Test
        fun `GIVEN valid schema and environments with selected environment WHEN process THEN returns environments`() =
            runTest {
                val expectedEnvironmentsProcessorResult = EnvironmentsProcessorResult.Success(
                    environmentsDirectoryPath = environmentsDirectory.absolutePath,
                    selectedEnvironmentName = LOCAL_ENVIRONMENT_NAME,
                    environments = setOf(TestData.environment),
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
                        environmentsDirectoryPath = environmentsDirectory.absolutePath,
                        selectedEnvironmentName = LOCAL_ENVIRONMENT_NAME,
                        environments = setOf(
                            TestData.environment
                        )
                    )
                ),
                actual = environmentsProcessorResults,
            )
        }

        @Test
        fun `GIVEN invalid environments WHEN processRecursively THEN returns results list`() = runTest {
            val expectedException = Exception()
            createEnvironmentsDirectory()
            propertiesParser.propertiesParserResult = PropertiesParserResult.Failure(expectedException)

            val environmentsProcessorResults = defaultEnvironmentsProcessor.processRecursively(directory)

            assertEquals(
                expected = listOf(PropertiesSerialization(expectedException)),
                actual = environmentsProcessorResults,
            )
        }
    }

    @Test
    fun `WHEN addOrUpdateSelectedEnvironment THEN returns true`() {
        propertiesParser.addOrUpdateSelectedEnvironmentResult = true

        val addOrUpdateSelectedEnvironment =
            defaultEnvironmentsProcessor.addOrUpdateSelectedEnvironment(
                environmentsDirectory = directory,
                newSelectedEnvironment = LOCAL_ENVIRONMENT_NAME,
            )

        assertTrue(addOrUpdateSelectedEnvironment)
    }

    @Test
    fun `GIVEN environmentName WHEN environmentFileName THEN returns environment name`() {
        val environmentFileName = EnvironmentsProcessor.environmentFileName(LOCAL_ENVIRONMENT_NAME)
        assertEquals(expected = "local.environment.chamaleon.json", actual = environmentFileName)
    }

    @Test
    fun `WHEN addEnvironmentResult THEN returns true`() {
        environmentsParser.addEnvironmentsResult = true

        val addOrUpdateSelectedEnvironment =
            defaultEnvironmentsProcessor.addEnvironments(
                environmentsDirectory = directory,
                environments = emptySet(),
            )

        assertTrue(addOrUpdateSelectedEnvironment)
    }

    @ParameterizedTest
    @MethodSource("environmentFileMatcherTestData")
    fun `GIVEN environment file name WHEN matching THEN matches correctly`(
        expected: Boolean,
        environmentFileName: String,
    ) {
        val environmentFile = File(directory, environmentFileName)
        val actual = DefaultEnvironmentsProcessor.environmentFileMatcher(environmentFile)
        assertEquals(expected = expected, actual = actual)
    }

    @ParameterizedTest
    @MethodSource("environmentFileNameExtractorTestData")
    fun `GIVEN environmentFile WHEN extracting environment name THEN extracts correctly`(
        expected: String,
        environmentFileName: String
    ) {
        val environmentFile = File(directory, environmentFileName)
        val actual = DefaultEnvironmentsProcessor.environmentNameExtractor(environmentFile)
        assertEquals(expected = expected, actual = actual)
    }

    private fun createEnvironmentsDirectory(): File {
        val environmentsDirectory = File(directory, EnvironmentsProcessor.ENVIRONMENTS_DIRECTORY_NAME)
        environmentsDirectory.mkdir()
        return environmentsDirectory
    }

    companion object {
        private val localEnvironmentFileName = EnvironmentsProcessor.environmentFileName(LOCAL_ENVIRONMENT_NAME)
        private val productionEnvironmentFileName =
            EnvironmentsProcessor.environmentFileName(PRODUCTION_ENVIRONMENT_NAME)

        @JvmStatic
        fun environmentFileMatcherTestData(): List<Arguments> =
            listOf(
                // Valid
                Arguments.of(true, localEnvironmentFileName),
                Arguments.of(true, productionEnvironmentFileName),

                // Invalid environment file name
                Arguments.of(false, "local.environment.chamaleon.jso"),
                Arguments.of(false, "chamaleon.json"),
                Arguments.of(false, "local.json"),
                Arguments.of(false, "local.environment.json"),
                Arguments.of(false, "local.chamaleon.json"),
                Arguments.of(false, EnvironmentsProcessor.ENVIRONMENT_FILE_SUFFIX),
                Arguments.of(false, EnvironmentsProcessor.SCHEMA_FILE),
                Arguments.of(false, EnvironmentsProcessor.PROPERTIES_FILE),
            )

        @JvmStatic
        fun environmentFileNameExtractorTestData(): List<Arguments> =
            listOf(
                Arguments.of(LOCAL_ENVIRONMENT_NAME, localEnvironmentFileName),
                Arguments.of(PRODUCTION_ENVIRONMENT_NAME, productionEnvironmentFileName),
            )
    }
}