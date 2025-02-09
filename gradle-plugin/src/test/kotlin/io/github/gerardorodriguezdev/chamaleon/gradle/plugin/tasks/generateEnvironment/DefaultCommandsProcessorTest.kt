package io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment

import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Platform
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyValue
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.CommandParser.CommandParserResult
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.CommandsProcessor.CommandsProcessorResult.Failure
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.CommandsProcessor.CommandsProcessorResult.Success
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.testing.fakes.FakeCommandParser
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DefaultCommandsProcessorTest {
    private val commandParser = FakeCommandParser(commandParserResults = emptyList())
    private val defaultCommandsProcessor = DefaultCommandsProcessor(commandParser)

    @Test
    fun `GIVEN invalid command result WHEN process THEN returns error`() {
        commandParser.commandParserResults = listOf(invalidCommandParserResult)

        val result = defaultCommandsProcessor.process(invalidCommandsList)

        assertEquals(expected = Failure.InvalidCommand(EMPTY_STRING), actual = result)
    }

    @Test
    fun `GIVEN invalid platform type result WHEN process THEN returns error`() {
        commandParser.commandParserResults = listOf(invalidPlatformTypeCommandParserResult)

        val result = defaultCommandsProcessor.process(invalidCommandsList)

        assertEquals(
            expected = Failure.InvalidPlatformType(
                command = EMPTY_STRING,
                platformTypeString = EMPTY_STRING,
            ),
            actual = result
        )
    }

    @Test
    fun `GIVEN valid commands WHEN process THEN returns success`() {
        commandParser.commandParserResults = listOf(
            CommandParserResult.Success(environment = jvmEnvironment),
            CommandParserResult.Success(environment = wasmEnvironment),
        )

        val result = defaultCommandsProcessor.process(listOf("firstCommand", "secondCommand"))

        assertEquals(expected = Success(environments = setOf(mergedEnvironment)), actual = result)
    }

    private companion object {
        const val ENVIRONMENT_NAME = "local"
        const val PROPERTY_NAME = "host"
        const val STRING_PROPERTY_VALUE = "localhost"
        const val EMPTY_STRING = ""

        val invalidCommandsList = listOf("anyCommand")

        val propertyValue = PropertyValue.StringProperty(STRING_PROPERTY_VALUE)

        val property = Platform.Property(
            name = PROPERTY_NAME,
            value = propertyValue,
        )

        val jvmPlatform = Platform(
            platformType = PlatformType.JVM,
            properties = setOf(property)
        )

        val wasmPlatform = Platform(
            platformType = PlatformType.WASM,
            properties = setOf(property)
        )

        val jvmEnvironment = Environment(
            name = ENVIRONMENT_NAME,
            platforms = setOf(jvmPlatform)
        )

        val wasmEnvironment = Environment(
            name = ENVIRONMENT_NAME,
            platforms = setOf(wasmPlatform)
        )

        val mergedEnvironment = Environment(
            name = ENVIRONMENT_NAME,
            platforms = setOf(jvmPlatform, wasmPlatform),
        )

        val invalidCommandParserResult = CommandParserResult.Failure.InvalidCommand(EMPTY_STRING)
        val invalidPlatformTypeCommandParserResult =
            CommandParserResult.Failure.InvalidPlatformType(command = EMPTY_STRING, platformTypeString = EMPTY_STRING)
    }
}