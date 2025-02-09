package io.github.gerardorodriguezdev.chamaleon.gradle.plugin

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.ENVIRONMENTS_DIRECTORY_NAME
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.PROPERTIES_FILE
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.SCHEMA_FILE
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.*
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.EnvironmentsProcessorResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.EnvironmentsProcessorResult.Success
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

            is Failure.SchemaParsingError -> schemaParsingError.toErrorMessage()

            is Failure.EnvironmentsParsingError -> environmentsParsingError.toErrorMessage()

            is Failure.PropertiesParsingError -> propertiesParsingError.toErrorMessage()

            is Failure.PlatformsNotEqualToSchema ->
                "Platforms of environment '$environmentName' are not equal to schema"

            is Failure.PropertiesNotEqualToSchema ->
                "Properties on platform '$platformType' for environment '$environmentName' are not equal to schema"

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

    private fun SchemaParserResult.Failure.toErrorMessage(): String =
        when (this) {
            is SchemaParserResult.Failure.FileNotFound -> "'$SCHEMA_FILE' not found on '$path'"
            is SchemaParserResult.Failure.FileIsEmpty -> "'$SCHEMA_FILE' on '$path' is empty"
            is SchemaParserResult.Failure.Serialization ->
                "Schema parsing failed with error '${throwable.message}'"

            is SchemaParserResult.Failure.EmptySupportedPlatforms ->
                "'$SCHEMA_FILE' on '$path' has empty supported platforms"

            is SchemaParserResult.Failure.EmptyPropertyDefinitions ->
                "'$SCHEMA_FILE' on '$path' has empty property definitions"

            is SchemaParserResult.Failure.InvalidPropertyDefinition ->
                "'$SCHEMA_FILE' on '$path' contains invalid property definitions"

            is SchemaParserResult.Failure.DuplicatedPropertyDefinition ->
                "'$SCHEMA_FILE' on '$path' contains duplicated property definitions"
        }

    private fun EnvironmentsParserResult.Failure.toErrorMessage(): String =
        when (this) {
            is EnvironmentsParserResult.Failure.FileIsEmpty -> "Environments file on '$path' is empty"
            is EnvironmentsParserResult.Failure.EnvironmentNameEmpty -> "Environment name is empty on '$path'"
            is EnvironmentsParserResult.Failure.Serialization ->
                "Environment parsing failed with error '${throwable.message}'"
        }

    private fun PropertiesParserResult.Failure.toErrorMessage(): String =
        when (this) {
            is PropertiesParserResult.Failure.Serialization ->
                "Properties parsing failed with error '${throwable.message}'"
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

                if (addOrUpdateSelectedEnvironmentResult is AddEnvironmentsResult.Failure) {
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