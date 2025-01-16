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
        fun `GIVEN non null property definition is null WHEN process THEN returns failure`() = runTest {
            environmentsParser.environmentsParserResult = EnvironmentsParserResult.Success(
                environments = setOf(
                    TestData.validCompleteEnvironment.copy(
                        platforms = TestData.validCompleteEnvironment.platforms.map { platformDto ->
                            platformDto.copy(
                                properties = platformDto.properties.map { propertyDto ->
                                    if (propertyDto.name == "DOMAIN") {
                                        propertyDto.copy(value = null)
                                    } else propertyDto
                                }.toSet()
                            )
                        }.toSet()
                    )
                )
            )

            assertIs<NullPropertyNotNullableOnSchema>(
                defaultEnvironmentsProcessor.process(directory)
            )
        }

        @Test
        fun `GIVEN selected environment not in environments WHEN process THEN returns failure`() = runTest {
            propertiesParser.propertiesParserResult =
                PropertiesParserResult.Success(selectedEnvironmentName = "NonExisting")

            assertIs<SelectedEnvironmentInvalid>(
                defaultEnvironmentsProcessor.process(directory)
            )
        }

        @Test
        fun `GIVEN valid schema and environments with selected environment WHEN process THEN returns environments`() =
            runTest {
                val expectedEnvironmentsProcessorResult = EnvironmentsProcessor.EnvironmentsProcessorResult.Success(
                    environmentsDirectoryPath = directory.absolutePath,
                    selectedEnvironmentName = TestData.ENVIRONMENT_NAME,
                    environments = setOf(TestData.validCompleteEnvironment),
                )

                val environmentsProcessorResult = defaultEnvironmentsProcessor.process(directory)

                assertEquals(environmentsProcessorResult, expectedEnvironmentsProcessorResult)
            }
    }

    @Nested
    inner class ProcessRecursively {
        @Test
        fun `GIVEN valid environments WHEN processRecursively THEN returns results list`() = runTest {
            val environmentsDirectory = environmentsDirectory()

            val environmentsProcessorResults = defaultEnvironmentsProcessor.processRecursively(directory)

            assertEquals(
                listOf(
                    EnvironmentsProcessor.EnvironmentsProcessorResult.Success(
                        environmentsDirectoryPath = environmentsDirectory.absolutePath,
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
            environmentsDirectory()
            propertiesParser.propertiesParserResult = PropertiesParserResult.Failure(expectedException)

            val environmentsProcessorResults = defaultEnvironmentsProcessor.processRecursively(directory)

            assertEquals(
                listOf(PropertiesSerialization(expectedException)),
                environmentsProcessorResults,
            )
        }
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

    @Test
    fun `GIVEN environmentName WHEN environmentFileName THEN returns environment name`() {
        val environmentFileName = EnvironmentsProcessor.environmentFileName("local")
        assertEquals(localEnvironmentFileName, environmentFileName)
    }

    @ParameterizedTest
    @MethodSource("fileNames")
    fun `GIVEN file name WHEN matching THEN matches correctly`(expected: Boolean, fileName: String) {
        val file = File(directory, fileName)
        val actual = DefaultEnvironmentsProcessor.environmentFileMatcher(file)
        assertEquals(expected, actual)
    }

    @ParameterizedTest
    @MethodSource("environmentNames")
    fun `GIVEN file WHEN extracting environment name THEN extracts correctly`(expected: String, fileName: String) {
        val file = File(directory, fileName)
        val actual = DefaultEnvironmentsProcessor.environmentFileNameExtractor(file)
        assertEquals(expected, actual)
    }

    private fun environmentsDirectory(): File {
        val environmentsDirectory = File(directory, EnvironmentsProcessor.ENVIRONMENTS_DIRECTORY_NAME)
        environmentsDirectory.mkdir()
        return environmentsDirectory
    }

    companion object {
        private const val LOCAL_ENVIRONMENT_NAME = "local"
        private const val PRODUCTION_ENVIRONMENT_NAME = "production"
        private val localEnvironmentFileName = EnvironmentsProcessor.environmentFileName(LOCAL_ENVIRONMENT_NAME)
        private val productionEnvironmentFileName =
            EnvironmentsProcessor.environmentFileName(PRODUCTION_ENVIRONMENT_NAME)

        @JvmStatic
        fun fileNames(): List<Arguments> =
            listOf(
                // Valid
                Arguments.of(true, localEnvironmentFileName),
                Arguments.of(true, productionEnvironmentFileName),

                // Invalid environment file name
                Arguments.of(false, "local.chamaleon.jso"),
                Arguments.of(false, "chamaleon.json"),
                Arguments.of(false, "local.json"),

                // Restricted file names
                Arguments.of(false, EnvironmentsProcessor.SCHEMA_FILE),
                Arguments.of(false, EnvironmentsProcessor.PROPERTIES_FILE),
            )

        @JvmStatic
        fun environmentNames(): List<Arguments> =
            listOf(
                Arguments.of(LOCAL_ENVIRONMENT_NAME, localEnvironmentFileName),
                Arguments.of(PRODUCTION_ENVIRONMENT_NAME, productionEnvironmentFileName),
            )
    }
}