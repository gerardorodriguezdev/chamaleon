package io.github.gerardorodriguezdev.chamaleon.gradle.plugin

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.ENVIRONMENTS_DIRECTORY_NAME
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.PROPERTIES_FILE
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.SCHEMA_FILE
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.EnvironmentsProcessorResult
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.EnvironmentsProcessorResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.EnvironmentsProcessorResult.Success
import kotlinx.coroutines.runBlocking
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.tasks.TaskProvider

public class GradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            createExtension()
            registerCreateSampleTask()
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
        val environmentsProcessor = EnvironmentsProcessor.create()
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
            is Failure.EnvironmentsDirectoryNotFound -> "'$ENVIRONMENTS_DIRECTORY_NAME' not found on '$environmentsDirectoryPath'"
            is Failure.SchemaFileNotFound -> "'$SCHEMA_FILE' not found on '$environmentsDirectoryPath'"
            is Failure.SchemaFileIsEmpty -> "'$SCHEMA_FILE' on '$environmentsDirectoryPath' is empty"
            is Failure.SchemaSerialization -> "Schema parsing failed with error '${throwable.message}'"
            is Failure.EnvironmentsSerialization -> "Environments parsing failed with error '${throwable.message}'"
            is Failure.PropertiesSerialization -> "Properties parsing failed with error '${throwable.message}'"
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

    private class GradlePluginException(message: String) : IllegalStateException(message)

    private fun Project.registerCreateSampleTask(): TaskProvider<CreateSampleTask> =
        tasks.register(CREATE_SAMPLE_TASK_NAME, CreateSampleTask::class.java) {
            environmentsDirectory.set(environmentsDirectory())
        }

    internal companion object {
        const val EXTENSION_NAME = "chamaleon"
        const val CREATE_SAMPLE_TASK_NAME = "chamaleonCreateSample"
    }
}