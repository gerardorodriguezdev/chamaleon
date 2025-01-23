package io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment

import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Platform
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyValue
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.GenerateEnvironmentCommandParser.GenerateEnvironmentCommandParserResult
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.GenerateEnvironmentCommandParser.GenerateEnvironmentCommandParserResult.Failure
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.GenerateEnvironmentCommandParser.GenerateEnvironmentCommandParserResult.Success
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

internal class GenerateEnvironmentCommandParserTest {

    @ParameterizedTest
    @MethodSource("testCases")
    fun `GIVEN command WHEN parse THEN returns result`(generateEnvironmentCommandTestCase: GenerateEnvironmentCommandTestCase) {
        val parsingResult = GenerateEnvironmentCommandParser().parse(generateEnvironmentCommandTestCase.command)
        assertEquals(expected = generateEnvironmentCommandTestCase.expectedResult, actual = parsingResult)
    }

    internal companion object {
        const val PRODUCTION_ENVIRONMENT_NAME = "production"
        const val HOST_PROPERTY_NAME = "host"
        const val HOST_PROPERTY_VALUE = "productionhost"

        val VALID_COMMAND =
            """
                chamaleonEnvironment="$PRODUCTION_ENVIRONMENT_NAME.jvm.properties[$HOST_PROPERTY_NAME=$HOST_PROPERTY_VALUE]"
            """.trimIndent()

        @JvmStatic
        fun testCases(): List<GenerateEnvironmentCommandTestCase> =
            listOf(
                invalidCommandTestCase("""chamaleonEnvironment="production.jvm.properties[name=value,name=value""""),
                invalidCommandTestCase("""chamaleonEnvironment="production.jvmproperties[name=value,name=value]""""),
                invalidCommandTestCase("""chamaleonEnvironment="production.properties[name=value,name=value]""""),
                invalidCommandTestCase("""chamaleonEnvironment="jvm.properties[name=value,name=value]""""),
                invalidCommandTestCase("""chamaleonEnvironment=".jvm.properties[name=value,name=value]""""),
                invalidCommandTestCase("""="production.jvm.properties[name=value,name=value]""""),
                invalidCommandTestCase("""chamaleonEnvironment"production.jvm.properties[name=value,name=value]""""),
                invalidCommandTestCase("""chamaleonnvironment="production.jvm.properties[name=value,name=value]""""),
                invalidCommandTestCase("""chamaleonEnvironment=production.jvm.properties[name=value,name=value]"""),
                invalidCommandTestCase("""chamaleonEnvironment="production.jvm""""),
                invalidCommandTestCase("""chamaleonEnvironment="production.jvm.properties""""),
                invalidCommandTestCase("""chamaleonEnvironment="production.jvm.properties["""),
                invalidCommandTestCase("""chamaleonEnvironment="production.jvm.properties[]""""),
                invalidCommandTestCase("""chamaleonEnvironment="production.jvm.properties[,]""""),
                invalidCommandTestCase("""chamaleonEnvironment="production.jvm.[name=value,name=value]""""),
                invalidCommandTestCase("""chamaleonEnvironmen="production.jvm.properties[name=value,name=value]""""),
                invalidCommandTestCase("""chamaleonEnvironment="production.jvm.properties name=value,name=value """"),

                // Invalid platform type
                invalidPlatformTypeTestCase(
                    command = """chamaleonEnvironment="production..properties[name=value,name=value]"""",
                    platformTypeString = "",
                ),
                invalidPlatformTypeTestCase(
                    command = """chamaleonEnvironment="production.jv.properties[name=value,name=value]"""",
                    platformTypeString = "jv",
                ),

                // Valid case
                GenerateEnvironmentCommandTestCase(
                    command = VALID_COMMAND,
                    expectedResult = Success(
                        environment = Environment(
                            name = PRODUCTION_ENVIRONMENT_NAME,
                            platforms = setOf(
                                Platform(
                                    platformType = PlatformType.JVM,
                                    properties = setOf(
                                        Platform.Property(
                                            name = HOST_PROPERTY_NAME,
                                            value = PropertyValue.StringProperty(
                                                value = HOST_PROPERTY_VALUE,
                                            )
                                        )
                                    ),
                                )
                            ),
                        )
                    ),
                ),
            )

        private fun invalidCommandTestCase(invalidCommand: String): GenerateEnvironmentCommandTestCase =
            GenerateEnvironmentCommandTestCase(
                command = invalidCommand,
                expectedResult = Failure.InvalidCommand(invalidCommand),
            )

        private fun invalidPlatformTypeTestCase(
            command: String,
            platformTypeString: String
        ): GenerateEnvironmentCommandTestCase =
            GenerateEnvironmentCommandTestCase(
                command = command,
                expectedResult = Failure.InvalidPlatformType(platformTypeString),
            )

        data class GenerateEnvironmentCommandTestCase(
            val command: String,
            val expectedResult: GenerateEnvironmentCommandParserResult,
        )
    }

}