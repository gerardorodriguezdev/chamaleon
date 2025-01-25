package io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment

import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Platform
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyValue
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.CommandParser.CommandParserResult
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.CommandParser.CommandParserResult.Failure
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.CommandParser.CommandParserResult.Success
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

internal class DefaultCommandParserTest {

    @ParameterizedTest
    @MethodSource("testCases")
    fun `GIVEN command WHEN parse THEN returns result`(commandParserTestCase: CommandParserTestCase) {
        val parsingResult = DefaultCommandParser().parse(commandParserTestCase.command)
        assertEquals(expected = commandParserTestCase.expectedResult, actual = parsingResult)
    }

    internal companion object {
        const val PRODUCTION_ENVIRONMENT_NAME = "production"
        const val HOST_PROPERTY_NAME = "host"
        const val HOST_PROPERTY_VALUE = "productionhost"
        const val VALID_COMMAND =
            "$PRODUCTION_ENVIRONMENT_NAME.jvm.properties[$HOST_PROPERTY_NAME=$HOST_PROPERTY_VALUE]"

        @JvmStatic
        fun testCases(): List<CommandParserTestCase> =
            listOf(
                // Invalid command
                invalidCommandTestCase("production.jvm.properties[host=productionhost,isDebug=true"),
                invalidCommandTestCase("production.jvm.properties[hostproductionhost]"),
                invalidCommandTestCase("production.jvm.propertieshostproductionhost]"),
                invalidCommandTestCase("production.jvm.properties"),
                invalidCommandTestCase("production.jvm.properties["),
                invalidCommandTestCase("production.jvm.[host=productionhost,isDebug=true]"),
                invalidCommandTestCase("production.jvm..[host=productionhost,isDebug=true]"),
                invalidCommandTestCase("..jvm.properties[host=productionhost,isDebug=true]"),
                invalidCommandTestCase(".jvm.properties[host=productionhost,isDebug=true]"),
                invalidCommandTestCase("production.jvm.other[host=productionhost,isDebug=true]"),
                invalidCommandTestCase("production.jvm.properties[=productionhost]"),
                invalidCommandTestCase("production.jvm.properties[host=]"),

                // Invalid platform type
                invalidPlatformTypeTestCase(
                    command = "production..properties[name=value,name=value]",
                    platformTypeString = "",
                ),
                invalidPlatformTypeTestCase(
                    command = "production.jv.properties[name=value,name=value]",
                    platformTypeString = "jv",
                ),

                // Valid case
                CommandParserTestCase(
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

        private fun invalidCommandTestCase(invalidCommand: String): CommandParserTestCase =
            CommandParserTestCase(
                command = invalidCommand,
                expectedResult = Failure.InvalidCommand(invalidCommand),
            )

        private fun invalidPlatformTypeTestCase(
            command: String,
            platformTypeString: String
        ): CommandParserTestCase =
            CommandParserTestCase(
                command = command,
                expectedResult = Failure.InvalidPlatformType(
                    command = command,
                    platformTypeString = platformTypeString,
                ),
            )

        data class CommandParserTestCase(
            val command: String,
            val expectedResult: CommandParserResult,
        )
    }
}