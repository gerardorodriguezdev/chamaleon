package io.github.gerardorodriguezdev.chamaleon.gradle.plugin

import io.github.gerardorodriguezdev.chamaleon.core.models.Project.Companion.ENVIRONMENTS_DIRECTORY_NAME
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectDeserializationResult
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory.Companion.toUnsafeExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString.Companion.toNonEmptyString
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectDeserializer
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.extensions.ChamaleonExtension
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.mappers.toErrorMessage
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.GenerateSampleTask
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.SelectEnvironmentTask
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.GenerateEnvironmentTask
import kotlinx.coroutines.runBlocking
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.tasks.TaskProvider

public class ChamaleonGradlePlugin : Plugin<Project> {
    private val projectDeserializer = ProjectDeserializer.create()

    override fun apply(target: Project) {
        with(target) {
            val extension = createExtension()
            registerGenerateSampleTask()
            registerSelectEnvironmentTask(extension)
            registerGenerateEnvironmentTask(extension)
        }
    }

    private fun Project.createExtension(): ChamaleonExtension {
        val extension = extensions.create(EXTENSION_NAME, ChamaleonExtension::class.java)
        scanProject(extension)
        return extension
    }

    private fun Project.scanProject(extension: ChamaleonExtension) {
        when (val projectDeserializationResult = deserializeProject()) {
            is ProjectDeserializationResult.Success -> {
                extension.project.set(projectDeserializationResult.project)
                logger.info("Project deserialization successful at ${projectDeserializationResult.project.environmentsDirectory.path}")
            }

            is ProjectDeserializationResult.Failure ->
                throw ChamaleonGradlePluginException(message = projectDeserializationResult.toErrorMessage())
        }
    }

    private fun Project.deserializeProject(): ProjectDeserializationResult {
        return runBlocking {
            val environmentsExistingDirectory = environmentsExistingDirectory()
            projectDeserializer.deserialize(environmentsExistingDirectory)
        }
    }

    private fun Project.environmentsDirectory(): Directory = layout.projectDirectory.dir(ENVIRONMENTS_DIRECTORY_NAME)

    private fun Project.environmentsExistingDirectory(): ExistingDirectory =
        layout.projectDirectory.dir(ENVIRONMENTS_DIRECTORY_NAME).asFile.toUnsafeExistingDirectory()

    private fun Project.registerGenerateSampleTask(): TaskProvider<GenerateSampleTask> =
        tasks.register(GENERATE_SAMPLE_TASK_NAME, GenerateSampleTask::class.java) {
            val generateSampleCommandLineArgument =
                providers.gradleProperty(GENERATE_SAMPLE_COMMAND_LINE_ARGUMENT).orNull

            environmentsDirectory.set(
                if (generateSampleCommandLineArgument != null) {
                    layout.projectDirectory.dir(generateSampleCommandLineArgument)
                } else {
                    environmentsDirectory()
                }
            )
        }

    private fun Project.registerSelectEnvironmentTask(extension: ChamaleonExtension): TaskProvider<SelectEnvironmentTask> =
        tasks.register(SELECT_ENVIRONMENT_TASK_NAME, SelectEnvironmentTask::class.java) {
            val newSelectedEnvironmentNameString =
                providers.gradleProperty(SELECT_ENVIRONMENT_COMMAND_LINE_ARGUMENT).orNull

            if (newSelectedEnvironmentNameString == null) {
                newSelectedEnvironmentName.set(null)
            } else {
                val nonEmptyStringNewSelectedEnvironmentName = newSelectedEnvironmentNameString.toNonEmptyString()
                    ?: throw ChamaleonGradlePluginException("Selected environment name was empty")
                newSelectedEnvironmentName.set(nonEmptyStringNewSelectedEnvironmentName)
            }

            projectProperty.set(extension.project.get())
        }

    private fun Project.registerGenerateEnvironmentTask(extension: ChamaleonExtension): TaskProvider<GenerateEnvironmentTask> =
        tasks.register(GENERATE_ENVIRONMENT_TASK_NAME, GenerateEnvironmentTask::class.java) {
            val generateEnvironmentCommands =
                providers.gradlePropertiesPrefixedBy(GENERATE_ENVIRONMENT_COMMAND_LINE_ARGUMENT).orNull

            this.generateEnvironmentCommands.set(generateEnvironmentCommands?.values)

            this.projectProperty.set(extension.project)
        }

    private class ChamaleonGradlePluginException(message: String) : IllegalStateException(message)

    internal companion object {
        const val EXTENSION_NAME = "chamaleon"

        const val GENERATE_SAMPLE_TASK_NAME = "chamaleonGenerateSample"
        const val SELECT_ENVIRONMENT_TASK_NAME = "chamaleonSelectEnvironment"
        const val GENERATE_ENVIRONMENT_TASK_NAME = "chamaleonGenerateEnvironment"

        const val GENERATE_SAMPLE_COMMAND_LINE_ARGUMENT = "chamaleon.sampleOutputDirectory"
        const val SELECT_ENVIRONMENT_COMMAND_LINE_ARGUMENT = "chamaleon.newSelectedEnvironment"
        const val GENERATE_ENVIRONMENT_COMMAND_LINE_ARGUMENT = "chamaleon.environment"
    }
}