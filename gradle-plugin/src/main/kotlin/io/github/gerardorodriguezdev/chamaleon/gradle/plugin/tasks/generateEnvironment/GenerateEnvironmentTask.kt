package io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.models.Project
import io.github.gerardorodriguezdev.chamaleon.core.results.UpdateProjectResult
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore.Companion.toNonEmptyKeySetStore
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.CommandsProcessor.CommandsProcessorResult
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

@CacheableTask
public abstract class GenerateEnvironmentTask : DefaultTask() {
    private val environmentsProcessor = EnvironmentsProcessor.create()
    private val commandsProcessor = CommandsProcessor.create()

    @get:Input
    public abstract val generateEnvironmentCommands: ListProperty<String>

    @get:Input
    public abstract val project: Property<Project>

    @TaskAction
    public fun generateEnvironment() {
        val commands = generateEnvironmentCommands.get()
        val environments = environments(commands)
        generateEnvironments(environments)
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

    private fun generateEnvironments(environments: Set<Environment>) {
        val project = project.get()
        val environmentsNonEmptyKeyStore = environments.toNonEmptyKeySetStore()
        if (environmentsNonEmptyKeyStore == null) {
            throw GenerateEnvironmentTaskException(
                message = "Error generating environments file on '${project.environmentsDirectory.directory.path}'"
            )
        }

        val newProject = project.addEnvironments(
            newEnvironments = environmentsNonEmptyKeyStore,
        )

        if (newProject == null) {
            throw GenerateEnvironmentTaskException(
                message = "Error generating environments file on '${project.environmentsDirectory.directory.path}'"
            )
        }

        runBlocking {
            val updateProjectResult = environmentsProcessor.updateProject(newProject)
            if (updateProjectResult is UpdateProjectResult.Failure) {
                throw GenerateEnvironmentTaskException(
                    message = "Error generating environments file on '${project.environmentsDirectory.directory.path}'"
                )
            }
        }
    }

    private class GenerateEnvironmentTaskException(message: String) : Exception(message)
}