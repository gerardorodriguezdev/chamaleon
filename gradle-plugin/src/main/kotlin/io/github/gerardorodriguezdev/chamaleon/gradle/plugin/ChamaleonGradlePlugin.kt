package io.github.gerardorodriguezdev.chamaleon.gradle.plugin

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.PROPERTIES_FILE
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.SCHEMA_FILE
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.EnvironmentsProcessorResult
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.EnvironmentsProcessorResult.Failure
import kotlinx.coroutines.runBlocking
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory

public class ChamaleonGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val extension = extensions.create(EXTENSION_NAME, ChamaleonExtension::class.java)

            val environmentsProcessorResult = environmentsProcessorResult()

            when (environmentsProcessorResult) {
                is EnvironmentsProcessorResult.Success -> {
                    extension.environments.set(environmentsProcessorResult.environments)
                    extension.selectedEnvironmentName.set(environmentsProcessorResult.selectedEnvironmentName)
                }

                is Failure -> throw ChamaleonGradlePluginException(
                    message = environmentsProcessorResult.toErrorMessage()
                )
            }
        }
    }

    private fun Project.environmentsProcessorResult(): EnvironmentsProcessorResult {
        val environmentsProcessor = EnvironmentsProcessor.create()
        return runBlocking {
            val environmentsDirectory = environmentsDirectory()
            environmentsProcessor.process(environmentsDirectory.asFile)
        }
    }

    private fun Project.environmentsDirectory(): Directory =
        layout.projectDirectory.dir(EnvironmentsProcessor.ENVIRONMENTS_DIRECTORY_NAME)

    private fun Failure.toErrorMessage(): String =
        when (this) {
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

    private class ChamaleonGradlePluginException(message: String) : IllegalStateException(message)

    private companion object {
        const val EXTENSION_NAME = "chamaleon"
    }
}