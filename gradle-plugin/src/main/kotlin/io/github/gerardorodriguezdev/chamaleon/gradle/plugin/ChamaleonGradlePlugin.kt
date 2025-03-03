package io.github.gerardorodriguezdev.chamaleon.gradle.plugin

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.ENVIRONMENTS_DIRECTORY_NAME
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.PROPERTIES_FILE
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.SCHEMA_FILE
import io.github.gerardorodriguezdev.chamaleon.core.results.*
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsProcessorResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsProcessorResult.Success
import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.ExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.ExistingDirectory.Companion.toUnsafeExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.NonEmptyString.Companion.toNonEmptyString
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.extensions.ChamaleonExtension
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.GenerateSampleTask
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.GenerateEnvironmentTask
import kotlinx.coroutines.runBlocking
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.tasks.TaskProvider

@Suppress("TooManyFunctions")
public class ChamaleonGradlePlugin : Plugin<Project> {
    private val environmentsProcessor = EnvironmentsProcessor.create()

    override fun apply(target: Project) {
        with(target) {
            val extension = createExtension()
            registerGenerateSampleTask()
            registerSelectEnvironmentTask(extension)
            registerGenerateEnvironmentTask()
        }
    }

    private fun Project.createExtension(): ChamaleonExtension {
        val extension = extensions.create(EXTENSION_NAME, ChamaleonExtension::class.java)
        scanProject(extension)
        return extension
    }

    private fun Project.scanProject(extension: ChamaleonExtension) {
        val environmentsProcessorResult = environmentsProcessorResult()

        when (environmentsProcessorResult) {
            is Success -> handleSuccess(extension, environmentsProcessorResult)
            is Failure -> handleFailure(environmentsProcessorResult)
        }
    }

    private fun Project.environmentsProcessorResult(): EnvironmentsProcessorResult {
        return runBlocking {
            val environmentsExistingDirectory = environmentsExistingDirectory()
            environmentsProcessor.process(environmentsExistingDirectory)
        }
    }

    private fun Project.environmentsDirectory(): Directory = layout.projectDirectory.dir(ENVIRONMENTS_DIRECTORY_NAME)

    private fun Project.environmentsExistingDirectory(): ExistingDirectory =
        layout.projectDirectory.dir(ENVIRONMENTS_DIRECTORY_NAME).asFile.toUnsafeExistingDirectory()

    private fun handleSuccess(extension: ChamaleonExtension, success: Success) {
        extension.project.set(success.project)
    }

    private fun handleFailure(failure: Failure) {
        throw ChamaleonGradlePluginException(
            message = failure.toErrorMessage()
        )
    }

    @Suppress("Indentation")
    private fun Failure.toErrorMessage(): String =
        when (this) {
            is Failure.SchemaParsing -> error.toErrorMessage()

            is Failure.EnvironmentsParsing -> error.toErrorMessage()

            is Failure.PropertiesParsing -> error.toErrorMessage()

            is Failure.ProjectValidation -> error.toErrorMessage()

            is Failure.InvalidPropertiesFile -> "Invalid properties file at '$environmentsDirectoryPath'"
            is Failure.InvalidSchemaFile -> "Invalid schema file at '$environmentsDirectoryPath'"
        }

    private fun SchemaParserResult.Failure.toErrorMessage(): String =
        when (this) {
            is SchemaParserResult.Failure.FileIsEmpty -> "'$SCHEMA_FILE' on '$schemaFilePath' is empty"
            is SchemaParserResult.Failure.Serialization ->
                "Schema parsing failed with error '${throwable.message}'"
        }

    private fun EnvironmentsParserResult.Failure.toErrorMessage(): String =
        when (this) {
            is EnvironmentsParserResult.Failure.FileIsEmpty -> "Environments file on '$environmentsDirectoryPath' is empty"
            is EnvironmentsParserResult.Failure.Serialization ->
                "Environment parsing failed with error '${throwable.message}'"

            is EnvironmentsParserResult.Failure.InvalidEnvironmentFile ->
                "Invalid environments file on '$environmentsDirectoryPath' with path '${environmentFilePath}'"
        }

    private fun PropertiesParserResult.Failure.toErrorMessage(): String =
        when (this) {
            is PropertiesParserResult.Failure.Serialization ->
                "Properties parsing failed with error '${throwable.message}'"
        }

    private fun ProjectValidationResult.Failure.toErrorMessage(): String =
        when (this) {
            is ProjectValidationResult.Failure.EnvironmentMissingPlatforms ->
                "Platforms of environment '$environmentName' are not equal to schema"

            is ProjectValidationResult.Failure.PropertyNotEqualToPropertyDefinition ->
                "Properties on platform '$platformType' for environment '$environmentName' are not equal to schema"

            is ProjectValidationResult.Failure.PropertyTypeNotEqualToPropertyDefinition ->
                "Value of property '$propertyName' for platform '$platformType' " +
                        "on environment '$environmentName' doesn't match propertyType '$propertyType' on schema"

            is ProjectValidationResult.Failure.NullPropertyNotNullable ->
                "Value on property '$propertyName' for platform '$platformType' on environment " +
                        "'$environmentName' was null and is not marked as nullable on schema"

            is ProjectValidationResult.Failure.SelectedEnvironmentNotFound ->
                "Selected environment '$selectedEnvironmentName' on '$PROPERTIES_FILE' not present in any environment" +
                        "[$environmentNames]"

            is ProjectValidationResult.Failure.PlatformMissingProperties ->
                "Platforms of environment '$environmentName' are not equal to schema"
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

    private fun Project.registerSelectEnvironmentTask(extension: ChamaleonExtension): TaskProvider<Task> =
        tasks.register(SELECT_ENVIRONMENT_TASK_NAME) {
            val newSelectedEnvironment = providers.gradleProperty(SELECT_ENVIRONMENT_COMMAND_LINE_ARGUMENT).orNull

            doLast {
                val nonEmptyNewSelectedEnvironmentName = newSelectedEnvironment?.toNonEmptyString()
                if (nonEmptyNewSelectedEnvironmentName == null) {
                    throw ChamaleonGradlePluginException("")
                }

                val newProject = extension.project.get().updateProperties(
                    newSelectedEnvironmentName = nonEmptyNewSelectedEnvironmentName,
                )
                if (newProject == null) {
                    throw ChamaleonGradlePluginException("Error updating project")
                }

                runBlocking {
                    val updateProjectResult = environmentsProcessor.updateProject(newProject)

                    if (updateProjectResult is Failure) {
                        @Suppress("Indentation")
                        throw ChamaleonGradlePluginException(
                            "Error updating selected environment '$newSelectedEnvironment' on environments " + "directory ${newProject.environmentsDirectory}"
                        )
                    }
                }
            }
        }

    private fun Project.registerGenerateEnvironmentTask(): TaskProvider<GenerateEnvironmentTask> =
        tasks.register(GENERATE_ENVIRONMENT_TASK_NAME, GenerateEnvironmentTask::class.java) {
            val generateEnvironmentCommands =
                providers.gradlePropertiesPrefixedBy(GENERATE_ENVIRONMENT_COMMAND_LINE_ARGUMENT).orNull

            this.generateEnvironmentCommands.set(generateEnvironmentCommands?.values)
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