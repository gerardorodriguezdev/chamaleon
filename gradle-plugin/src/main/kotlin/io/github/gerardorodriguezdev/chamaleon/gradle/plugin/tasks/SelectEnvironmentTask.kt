package io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks

import io.github.gerardorodriguezdev.chamaleon.core.models.Project
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectSerializationResult
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectSerializer
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.mappers.toErrorMessage
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

@CacheableTask
public abstract class SelectEnvironmentTask : DefaultTask() {
    private val projectSerializer = ProjectSerializer.create()

    @get:Input
    public abstract val newSelectedEnvironmentName: Property<NonEmptyString?>

    @get:Input
    public abstract val projectProperty: Property<Project>

    @TaskAction
    public fun selectEnvironment() {
        val newSelectedEnvironmentName = newSelectedEnvironmentName.get()
        val currentProject = projectProperty.get()

        val newProject = updateProperties(currentProject, newSelectedEnvironmentName)

        newProject.serialize()
    }

    private fun updateProperties(currentProject: Project, newSelectedEnvironmentName: NonEmptyString?): Project {
        val newProject = currentProject.updateProperties(newSelectedEnvironmentName = newSelectedEnvironmentName)

        if (newProject == null) {
            throw SelectEnvironmentTaskException(
                error = "Selected environment not found on existing environments '${currentProject.environments}'",
            )
        }

        return newProject
    }

    private fun Project.serialize() {
        runBlocking {
            when (val updateProjectResult = projectSerializer.serialize(this@serialize)) {
                is ProjectSerializationResult.Success -> logger.info("Environment selected successfully at '${environmentsDirectory.path}'")
                is ProjectSerializationResult.Failure -> throw SelectEnvironmentTaskException(updateProjectResult.toErrorMessage())
            }
        }
    }

    private class SelectEnvironmentTaskException(error: String) : IllegalStateException(error)
}