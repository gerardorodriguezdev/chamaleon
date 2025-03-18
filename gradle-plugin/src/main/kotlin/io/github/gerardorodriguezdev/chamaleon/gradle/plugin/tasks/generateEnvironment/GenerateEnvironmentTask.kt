package io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment

import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.models.Project
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectSerializationResult
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectSerializer
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.extensions.chamaleonLog
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.mappers.toErrorMessage
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
    public abstract val projectProperty: Property<Project>

    @TaskAction
    public fun generateEnvironment() {
        val commands = generateEnvironmentCommands.get()
        val project = projectProperty.get()

        val environments = commands.toEnvironments()
        val newProject = updateEnvironments(project, environments)

        newProject.serialize()
    }

    private fun List<String>.toEnvironments(): NonEmptyKeySetStore<String, Environment> =
        when (val commandsParserResult = commandsParser.parse(this)) {
            is CommandParserResult.Success -> commandsParserResult.environments
            is CommandParserResult.Failure ->
                throw GenerateEnvironmentTaskException(errorMessage = commandsParserResult.errorMessage)
        }

    private fun updateEnvironments(
        currentProject: Project,
        environments: NonEmptyKeySetStore<String, Environment>
    ): Project {
        val newProject = currentProject.addEnvironments(newEnvironments = environments)

        if (newProject == null) {
            throw GenerateEnvironmentTaskException(
                errorMessage =
                    "Environments couldn't be added to existing project on '${currentProject.environmentsDirectory.path}'"
            )
        }

        return newProject
    }

    private fun Project.serialize() {
        runBlocking {
            when (val updateProjectResult = projectSerializer.serialize(this@serialize)) {
                is ProjectSerializationResult.Success ->
                    logger.chamaleonLog("Environments generated successfully at '${environmentsDirectory.path}'")

                is ProjectSerializationResult.Failure ->
                    throw GenerateEnvironmentTaskException(errorMessage = updateProjectResult.toErrorMessage())
            }
        }
    }

    private class GenerateEnvironmentTaskException(errorMessage: String) : IllegalStateException(errorMessage)
}