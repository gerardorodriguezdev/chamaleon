package io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks

import io.github.gerardorodriguezdev.chamaleon.core.models.*
import io.github.gerardorodriguezdev.chamaleon.core.models.Project.Companion.projectOf
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema.Companion.schemaOf
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectSerializationResult
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory.Companion.toUnsafeExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore.Companion.toUnsafeNonEmptyKeyStore
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptySet.Companion.toUnsafeNonEmptySet
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString.Companion.toUnsafeNonEmptyString
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectSerializer
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.mappers.toErrorMessage
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

@CacheableTask
public abstract class GenerateSampleTask : DefaultTask() {
    private val projectSerializer = ProjectSerializer.create()

    @get:OutputDirectory
    public abstract val environmentsDirectory: DirectoryProperty

    @TaskAction
    public fun generateSample() {
        val environmentsDirectory = environmentsDirectory.get().asFile.toUnsafeExistingDirectory()

        runBlocking {
            val sampleProject = sampleProject(environmentsDirectory)

            sampleProject.serialize()
        }
    }

    private fun Project.serialize() {
        runBlocking {
            when (val updateProjectResult = projectSerializer.serialize(this@serialize)) {
                is ProjectSerializationResult.Success -> logger.info("Sample generated successfully at '${environmentsDirectory.path}'")
                is ProjectSerializationResult.Failure -> throw GenerateSampleTaskException(updateProjectResult.toErrorMessage())
            }
        }
    }

    private class GenerateSampleTaskException(error: String) : IllegalStateException(error)

    internal companion object {
        const val PROPERTY_NAME = "YourPropertyName"

        const val LOCAL_ENVIRONMENT_NAME = "local"
        const val LOCAL_ENVIRONMENT_PROPERTY_VALUE = "YourPropertyValueForLocalEnvironment"

        const val PRODUCTION_ENVIRONMENT_NAME = "production"
        const val PRODUCTION_ENVIRONMENT_PROPERTY_VALUE = "YourPropertyValueForProductionEnvironment"

        fun sampleProject(environmentsDirectory: ExistingDirectory): Project =
            requireNotNull(
                projectOf(
                    environmentsDirectory = environmentsDirectory,
                    schema = sampleSchema,
                    properties = sampleProperties,
                    environments = sampleEnvironments,
                ).project()
            )

        val sampleSchema =
            requireNotNull(
                schemaOf(
                    globalSupportedPlatformTypes = setOf(PlatformType.JVM).toUnsafeNonEmptySet(),
                    propertyDefinitions = setOf(
                        Schema.PropertyDefinition(
                            name = PROPERTY_NAME.toUnsafeNonEmptyString(),
                            propertyType = PropertyType.STRING,
                        )
                    ).toUnsafeNonEmptyKeyStore(),
                ).schema()
            )

        val sampleProperties = Properties(
            selectedEnvironmentName = LOCAL_ENVIRONMENT_NAME.toUnsafeNonEmptyString(),
        )

        val sampleLocalEnvironment =
            sampleEnvironment(
                environmentName = LOCAL_ENVIRONMENT_NAME,
                propertyValue = LOCAL_ENVIRONMENT_PROPERTY_VALUE,
            )

        val sampleProductionEnvironment =
            sampleEnvironment(
                environmentName = PRODUCTION_ENVIRONMENT_NAME,
                propertyValue = PRODUCTION_ENVIRONMENT_PROPERTY_VALUE,
            )

        val sampleEnvironments = setOf(
            sampleLocalEnvironment,
            sampleProductionEnvironment,
        ).toUnsafeNonEmptyKeyStore()

        private fun sampleEnvironment(
            environmentName: String,
            propertyValue: String,
        ): Environment =
            Environment(
                name = environmentName.toUnsafeNonEmptyString(),
                platforms = setOf(
                    Platform(
                        platformType = PlatformType.JVM,
                        properties = setOf(
                            Platform.Property(
                                name = PROPERTY_NAME.toUnsafeNonEmptyString(),
                                value = PropertyValue.StringProperty(
                                    propertyValue.toUnsafeNonEmptyString()
                                ),
                            )
                        ).toUnsafeNonEmptyKeyStore(),
                    )
                ).toUnsafeNonEmptyKeyStore(),
            )
    }
}