package io.github.gerardorodriguezdev.chamaleon.gradle.plugin

import io.github.gerardorodriguezdev.chamaleon.core.Versions
import io.github.gerardorodriguezdev.chamaleon.core.models.Project.Companion.ENVIRONMENTS_DIRECTORY_NAME
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectDeserializationResult
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory.Companion.toExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString.Companion.toNonEmptyString
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectDeserializer
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.extensions.ChamaleonExtension
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.extensions.chamaleonLog
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.mappers.toErrorMessage
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.GenerateSampleTask
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.SelectEnvironmentTask
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.GenerateEnvironmentTask
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
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
            registerVersionTask()
        }
    }

    private fun Project.createExtension(): ChamaleonExtension {
        val extension = extensions.create(EXTENSION_NAME, ChamaleonExtension::class.java)
        scanProject(extension)
        return extension
    }

    @Suppress("Indentation")
    private fun Project.scanProject(extension: ChamaleonExtension) {
        when (val projectDeserializationResult = deserializeProject()) {
            null -> logger.chamaleonLog("No project found on deserialization")

            is ProjectDeserializationResult.Success -> {
                extension.project.set(projectDeserializationResult.project)
                logger.chamaleonLog(
                    "Project deserialization " +
                        "successful at '${projectDeserializationResult.project.environmentsDirectory.path}'"
                )
            }

            is ProjectDeserializationResult.Failure ->
                throw ChamaleonGradlePluginException(errorMessage = projectDeserializationResult.toErrorMessage())
        }
    }

    private fun Project.deserializeProject(): ProjectDeserializationResult? {
        return runBlocking {
            val environmentsExistingDirectory = environmentsExistingDirectory()
            environmentsExistingDirectory?.let {
                projectDeserializer.deserialize(environmentsExistingDirectory)
            }
        }
    }

    private fun Project.registerGenerateSampleTask(): TaskProvider<GenerateSampleTask> =
        tasks.register(GENERATE_SAMPLE_TASK_NAME, GenerateSampleTask::class.java) {
            group = EXTENSION_NAME

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

    private fun Project.registerSelectEnvironmentTask(
        extension: ChamaleonExtension,
    ): TaskProvider<SelectEnvironmentTask> =
        tasks.register(SELECT_ENVIRONMENT_TASK_NAME, SelectEnvironmentTask::class.java) {
            group = EXTENSION_NAME

            val newSelectedEnvironmentNameString =
                providers.gradleProperty(SELECT_ENVIRONMENT_COMMAND_LINE_ARGUMENT).orNull

            if (newSelectedEnvironmentNameString == null) {
                newSelectedEnvironmentName.set(null)
            } else {
                val nonEmptyStringNewSelectedEnvironmentName = newSelectedEnvironmentNameString.toNonEmptyString()
                if (nonEmptyStringNewSelectedEnvironmentName == null) {
                    throw ChamaleonGradlePluginException(errorMessage = "Selected environment name was empty")
                }
                newSelectedEnvironmentName.set(nonEmptyStringNewSelectedEnvironmentName)
            }

            projectProperty.set(extension.project.get())
        }

    private fun Project.registerGenerateEnvironmentTask(
        extension: ChamaleonExtension,
    ): TaskProvider<GenerateEnvironmentTask> =
        tasks.register(GENERATE_ENVIRONMENT_TASK_NAME, GenerateEnvironmentTask::class.java) {
            group = EXTENSION_NAME

            val generateEnvironmentCommands =
                providers.gradlePropertiesPrefixedBy(GENERATE_ENVIRONMENT_COMMAND_LINE_ARGUMENT).orNull

            this.generateEnvironmentCommands.set(generateEnvironmentCommands?.values)

            projectProperty.set(extension.project)
        }

    private fun Project.registerVersionTask(): TaskProvider<DefaultTask> =
        tasks.register(VERSION_TASK_NAME, DefaultTask::class.java) {
            group = EXTENSION_NAME

            logger.chamaleonLog("Version: ${Versions.CORE}")
        }

    private fun Project.environmentsDirectory(): Directory = layout.projectDirectory.dir(ENVIRONMENTS_DIRECTORY_NAME)

    private fun Project.environmentsExistingDirectory(): ExistingDirectory? =
        environmentsDirectory().asFile.toExistingDirectory()

    private class ChamaleonGradlePluginException(errorMessage: String) : IllegalStateException(errorMessage)

    internal companion object {
        const val EXTENSION_NAME = "chamaleon"

        const val GENERATE_SAMPLE_TASK_NAME = "generateSample"
        const val SELECT_ENVIRONMENT_TASK_NAME = "selectEnvironment"
        const val GENERATE_ENVIRONMENT_TASK_NAME = "generateEnvironment"
        const val VERSION_TASK_NAME = "version"

        const val GENERATE_SAMPLE_COMMAND_LINE_ARGUMENT = "chamaleon.outputDirectory"
        const val SELECT_ENVIRONMENT_COMMAND_LINE_ARGUMENT = "chamaleon.newSelectedEnvironment"
        const val GENERATE_ENVIRONMENT_COMMAND_LINE_ARGUMENT = "chamaleon.environment"
    }
}