package io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment

import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.models.Project
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectSerializationResult
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectSerializer
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.CommandParser.CommandParserResult
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

@CacheableTask
public abstract class GenerateEnvironmentTask : DefaultTask() {
    private val projectSerializer = ProjectSerializer.create()
    private val commandsParser = CommandParser.create()

    @get:Input
    public abstract val generateEnvironmentCommands: ListProperty<String>

    @get:Input
    public abstract val project: Property<Project>

    @TaskAction
    public fun generateEnvironment() {
        val commands = generateEnvironmentCommands.get()
        val environments = commands.toEnvironments()
        val newProject = environments.addToExistingProject()

        newProject.serialize()
    }

    private fun List<String>.toEnvironments(): NonEmptyKeySetStore<String, Environment> =
        when (val commandsParserResult = commandsParser.parse(this)) {
            is CommandParserResult.Success -> commandsParserResult.environments
            is CommandParserResult.Failure ->
                throw GenerateEnvironmentTaskException(message = commandsParserResult.error)
        }

    private fun NonEmptyKeySetStore<String, Environment>.addToExistingProject(): Project {
        val project = project.get()

        val newProject = project.addEnvironments(newEnvironments = this)

        if (newProject == null) {
            throw GenerateEnvironmentTaskException(
                message = "Environments couldn't be added to existing project on '${project.environmentsDirectory.path}'"
            )
        }

        return newProject
    }

    private fun Project.serialize() =
        runBlocking {
            when (val updateProjectResult = projectSerializer.serialize(this@serialize)) {
                is ProjectSerializationResult.Success ->
                    logger.info("Environments generated successfully at '${environmentsDirectory.path}'")

                is ProjectSerializationResult.Failure ->
                    throw GenerateEnvironmentTaskException(
                        message = updateProjectResult.toErrorMessage(),
                    )
            }
        }

    private fun ProjectSerializationResult.Failure.toErrorMessage(): String =
        when (this) {
            is ProjectSerializationResult.Failure.InvalidPropertiesFile -> "Invalid properties file at '$environmentsDirectoryPath'"
            is ProjectSerializationResult.Failure.InvalidEnvironmentFile -> "Invalid environment file named '$environmentFileName' at '$environmentsDirectoryPath'"
            is ProjectSerializationResult.Failure.InvalidSchemaFile -> "Invalid properties file at '$environmentsDirectoryPath'"
            is ProjectSerializationResult.Failure.Serialization -> "Project serialization failed with error: '$error' at '$environmentsDirectoryPath'"
        }

    private class GenerateEnvironmentTaskException(message: String) : Exception(message)
}