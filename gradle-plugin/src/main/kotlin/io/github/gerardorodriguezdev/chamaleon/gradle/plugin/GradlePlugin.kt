package io.github.gerardorodriguezdev.chamaleon.gradle.plugin

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.ENVIRONMENTS_DIRECTORY_NAME
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.PROPERTIES_FILE
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.SCHEMA_FILE
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessorResult
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessorResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessorResult.Success
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.extensions.Extension
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.GenerateSampleTask
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.GenerateEnvironmentTask
import kotlinx.coroutines.runBlocking
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.tasks.TaskProvider

@Suppress("TooManyFunctions")
public class GradlePlugin : Plugin<Project> {
    private val environmentsProcessor = EnvironmentsProcessor.create()

    override fun apply(target: Project) {
        with(target) {
            createExtension()
            registerGenerateSampleTask()
            registerSelectEnvironmentTask()
            registerGenerateEnvironmentTask()
        }
    }

    private fun Project.createExtension(): Extension {
        val extension = extensions.create(EXTENSION_NAME, Extension::class.java)
        scanProject(extension)
        return extension
    }

    private fun Project.scanProject(extension: Extension) {
        val environmentsProcessorResult = environmentsProcessorResult()

        when (environmentsProcessorResult) {
            is Success -> handleSuccess(extension, environmentsProcessorResult)
            is Failure -> handleFailure(environmentsProcessorResult)
        }
    }

    private fun Project.environmentsProcessorResult(): EnvironmentsProcessorResult {
        return runBlocking {
            val environmentsDirectory = environmentsDirectory()
            environmentsProcessor.process(environmentsDirectory.asFile)
        }
    }

    private fun Project.environmentsDirectory(): Directory = layout.projectDirectory.dir(ENVIRONMENTS_DIRECTORY_NAME)

    private fun handleSuccess(extension: Extension, success: Success) {
        extension.environments.set(success.environments)
        extension.selectedEnvironmentName.set(success.selectedEnvironmentName)
    }

    private fun handleFailure(failure: Failure) {
        when (failure) {
            is Failure.EnvironmentsDirectoryNotFound -> Unit
            else -> throw GradlePluginException(
                message = failure.toErrorMessage()
            )
        }
    }

    @Suppress("Indentation")
    private fun Failure.toErrorMessage(): String =
        when (this) {
            is Failure.EnvironmentsDirectoryNotFound ->
                "'$ENVIRONMENTS_DIRECTORY_NAME' not found on '$environmentsDirectoryPath'"

            is Failure.SchemaFileNotFound -> "'$SCHEMA_FILE' not found on '$environmentsDirectoryPath'"
            is Failure.SchemaFileIsEmpty -> "'$SCHEMA_FILE' on '$environmentsDirectoryPath' is empty"
            is Failure.SchemaSerialization -> "Schema parsing failed with error '${throwable.message}'"
            is Failure.EnvironmentsSerialization -> "Environments parsing failed with error '${throwable.message}'"
            is Failure.PropertiesSerialization -> "Properties parsing failed with error '${throwable.message}'"
            is Failure.PlatformsNotEqualToSchema ->
                "Platforms of environment '$environmentName' are not equal to schema"

            is Failure.PropertiesNotEqualToSchema ->
                "Properties on platform '$platformType' for environment '$environmentName' are not equal to schema"

            is Failure.PropertyOnSchemaContainsUnsupportedPlatforms ->
                "Property $propertyName on schema on $environmentsDirectoryPath contains unsupported platforms"

            is Failure.PropertyTypeNotMatchSchema ->
                "Value of property '$propertyName' for platform '$platformType' " +
                        "on environment '$environmentName' doesn't match propertyType '$propertyType' on schema"

            is Failure.NullPropertyNotNullableOnSchema ->
                "Value on property '$propertyName' for platform '$platformType' on environment " +
                        "'$environmentName' was null and is not marked as nullable on schema"

            is Failure.SelectedEnvironmentInvalid ->
                "Selected environment '$selectedEnvironmentName' on '$PROPERTIES_FILE' not present in any environment" +
                        "[$environmentNames]"
        }

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

    private fun Project.registerSelectEnvironmentTask(): TaskProvider<Task> =
        tasks.register(SELECT_ENVIRONMENT_TASK_NAME) {
            val newSelectedEnvironment = providers.gradleProperty(SELECT_ENVIRONMENT_COMMAND_LINE_ARGUMENT).orNull
            val environmentsDirectory = environmentsDirectory()

            doLast {
                val addOrUpdateSelectedEnvironmentResult = environmentsProcessor.addOrUpdateSelectedEnvironment(
                    environmentsDirectory = environmentsDirectory.asFile,
                    newSelectedEnvironment = newSelectedEnvironment
                )

                if (!addOrUpdateSelectedEnvironmentResult) {
                    @Suppress("Indentation")
                    throw GradlePluginException(
                        message = "Error updating selected environment '$newSelectedEnvironment' on environments " +
                                "directory $environmentsDirectory"
                    )
                }
            }
        }

    private fun Project.registerGenerateEnvironmentTask(): TaskProvider<GenerateEnvironmentTask> =
        tasks.register(GENERATE_ENVIRONMENT_TASK_NAME, GenerateEnvironmentTask::class.java) {
            val environmentsDirectory = environmentsDirectory()
            val generateEnvironmentCommands =
                providers.gradlePropertiesPrefixedBy(GENERATE_ENVIRONMENT_COMMAND_LINE_ARGUMENT).orNull

            this.environmentsDirectory.set(environmentsDirectory)
            this.generateEnvironmentCommands.set(generateEnvironmentCommands?.values)
        }

    private class GradlePluginException(message: String) : IllegalStateException(message)

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