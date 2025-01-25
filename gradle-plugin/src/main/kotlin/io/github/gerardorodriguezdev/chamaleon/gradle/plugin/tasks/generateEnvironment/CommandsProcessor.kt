package io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment

import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Platform
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.CommandParser.CommandParserResult
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.CommandsProcessor.CommandProcessorResult
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.CommandsProcessor.CommandProcessorResult.Failure
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.CommandsProcessor.CommandProcessorResult.Success

internal interface CommandsProcessor {
    fun process(commands: List<String>): CommandProcessorResult

    sealed interface CommandProcessorResult {
        data class Success(
            val environments: Set<Environment>,
        ) : CommandProcessorResult

        sealed interface Failure : CommandProcessorResult {
            data class InvalidCommand(val command: String) : Failure
            data class InvalidPlatformType(val command: String, val platformTypeString: String) : Failure
        }
    }

    companion object {
        fun create(): CommandsProcessor = DefaultCommandsProcessor(
            commandParser = DefaultCommandParser(),
        )
    }
}

internal class DefaultCommandsProcessor(
    private val commandParser: CommandParser,
) : CommandsProcessor {

    override fun process(commands: List<String>): CommandProcessorResult {
        val environments = commands
            .map { command ->
                val commandParserResult = commandParser.parse(command)
                when (commandParserResult) {
                    is CommandParserResult.Success -> commandParserResult.environment
                    is CommandParserResult.Failure -> return commandParserResult.toFailure()
                }
            }

        return Success(
            environments = environments.mergeDuplicatedEnvironments()
        )
    }

    private fun CommandParserResult.Failure.toFailure(): Failure =
        when (this) {
            is CommandParserResult.Failure.InvalidCommand -> Failure.InvalidCommand(command)
            is CommandParserResult.Failure.InvalidPlatformType -> Failure.InvalidPlatformType(
                command = command,
                platformTypeString = platformTypeString,
            )
        }

    private fun List<Environment>.mergeDuplicatedEnvironments(): Set<Environment> =
        this
            .groupBy { environment -> environment.name }
            .map { (environmentName, environments) ->
                Environment(
                    name = environmentName,
                    platforms = environments.mergePlatforms(),
                )
            }
            .toSet()

    private fun List<Environment>.mergePlatforms(): Set<Platform> =
        flatMap { environment -> environment.platforms }.toSet()
}