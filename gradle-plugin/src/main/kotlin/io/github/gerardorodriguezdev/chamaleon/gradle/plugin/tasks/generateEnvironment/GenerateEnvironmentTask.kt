package io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.AddEnvironmentsResult
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.CommandsProcessor.CommandsProcessorResult
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
public abstract class GenerateEnvironmentTask : DefaultTask() {
    private val environmentsProcessor = EnvironmentsProcessor.create()
    private val commandsProcessor = CommandsProcessor.create()

    @get:Input
    public abstract val generateEnvironmentCommands: ListProperty<String>

    @get:OutputDirectory
    public abstract val environmentsDirectory: DirectoryProperty

    @TaskAction
    public fun generateEnvironment() {
        val commands = generateEnvironmentCommands.get()
        val environments = environments(commands)
        generateEnvironments(environmentsDirectory.get().asFile, environments)
    }

    private fun environments(commands: List<String>): Set<Environment> {
        val commandsProcessorResult = commandsProcessor.process(commands)
        return when (commandsProcessorResult) {
            is CommandsProcessorResult.Success -> commandsProcessorResult.environments
            is CommandsProcessorResult.Failure ->
                throw GenerateEnvironmentTaskException(
                    message = commandsProcessorResult.toMessage(),
                )
        }
    }

    private fun CommandsProcessorResult.Failure.toMessage(): String =
        when (this) {
            is CommandsProcessorResult.Failure.InvalidCommand -> "Command '$command' is not valid"
            is CommandsProcessorResult.Failure.InvalidPlatformType ->
                "Platform type '$platformTypeString' on $command is not valid"
        }

    private fun generateEnvironments(environmentsDirectory: File, environments: Set<Environment>) {
        val addEnvironmentsResult = environmentsProcessor.addEnvironments(
            environmentsDirectory = environmentsDirectory,
            environments = environments,
        )

        if (addEnvironmentsResult is AddEnvironmentsResult.Failure) {
            throw GenerateEnvironmentTaskException(
                message = "Error generating environments file on '${environmentsDirectory.path}'"
            )
        }
    }

    private class GenerateEnvironmentTaskException(message: String) : Exception(message)
}