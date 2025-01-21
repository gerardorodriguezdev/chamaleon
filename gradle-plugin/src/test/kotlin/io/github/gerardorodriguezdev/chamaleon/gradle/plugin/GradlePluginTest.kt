package io.github.gerardorodriguezdev.chamaleon.gradle.plugin

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Platform
import io.github.gerardorodriguezdev.chamaleon.core.entities.Platform.Property
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyValue.StringProperty
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.GradlePlugin.Companion.GENERATE_SAMPLE_TASK_NAME
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.SampleResources.writeAll
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class GradlePluginTest {
    @TempDir
    lateinit var directory: File

    @BeforeEach
    fun setUp() {
        createBuildFiles()
    }

    @Test
    fun `GIVEN plugin is applied to project WHEN task is executed THEN build is successful`() {
        createEnvironmentsFiles()

        val buildResult = helpTaskBuildResult()

        assertEquals(expected = TaskOutcome.SUCCESS, actual = buildResult.task(":help")?.outcome)
    }

    @Test
    fun `GIVEN plugin is applied to project WHEN project is configured THEN valid extension is returned`() {
        createEnvironmentsFiles()

        val project = buildProject()

        val extension = project.extension()
        assertEquals(expected = expectedEnvironments, actual = extension.environments.get())
        assertEquals(
            expected = LOCAL_ENVIRONMENT_NAME,
            actual = extension.selectedEnvironmentName.get()
        )
    }

    @Test
    fun `GIVEN plugin is applied to project WHEN generateSampleTask is executed THEN generates sample files`() {
        val buildResult = generateSampleTaskBuildResult()

        assertEquals(
            expected = TaskOutcome.SUCCESS,
            actual = buildResult.task(":$GENERATE_SAMPLE_TASK_NAME")?.outcome
        )
        val environmentsDirectory = environmentsDirectory()
        val environmentsFiles = environmentsDirectory.listFiles()
        assertEquals(expected = environmentsFiles.size, actual = SampleResources.resources.size)
        environmentsFiles.forEach { file ->
            val resource = SampleResources.resources.first { resource -> resource.fileName == file.name }
            assertEquals(expected = resource.fileContent, actual = file.readText())
        }
    }

    private fun createBuildFiles() {
        val buildFile = File(directory, BUILD_FILE_NAME)
        buildFile.writeText(buildFileContent)
    }

    private fun createEnvironmentsFiles() {
        val environmentsDirectory = environmentsDirectory()
        environmentsDirectory.mkdir()
        SampleResources.resources.writeAll(environmentsDirectory)
    }

    private fun environmentsDirectory(): File = File(directory, EnvironmentsProcessor.ENVIRONMENTS_DIRECTORY_NAME)

    private fun helpTaskBuildResult(): BuildResult =
        GradleRunner
            .create()
            .withProjectDir(directory)
            .withPluginClasspath()
            .withArguments("help")
            .build()

    private fun generateSampleTaskBuildResult(): BuildResult =
        GradleRunner
            .create()
            .withProjectDir(directory)
            .withPluginClasspath()
            .withArguments(GENERATE_SAMPLE_TASK_NAME)
            .build()

    private fun buildProject(configuration: Project.() -> Unit = {}): Project =
        ProjectBuilder
            .builder()
            .withProjectDir(directory)
            .build()
            .apply(configuration)

    private companion object {
        const val LOCAL_ENVIRONMENT_NAME = "local"
        const val LOCAL_ENVIRONMENT_PROPERTY_VALUE = "YourPropertyValueForLocalEnvironment"

        const val PRODUCTION_ENVIRONMENT_NAME = "production"
        const val PRODUCTION_ENVIRONMENT_PROPERTY_VALUE = "YourPropertyValueForProductionEnvironment"

        const val EXPECTED_PROPERTY_NAME = "YourPropertyName"

        const val BUILD_FILE_NAME = "build.gradle.kts"
        val buildFileContent =
            //language=kotlin
            """
                plugins {
                    id("io.github.gerardorodriguezdev.chamaleon")
                }
            """.trimIndent()

        val expectedEnvironments =
            setOf(
                Environment(
                    name = PRODUCTION_ENVIRONMENT_NAME,
                    platforms = setOf(expectedPlatform(PRODUCTION_ENVIRONMENT_PROPERTY_VALUE)),
                ),
                Environment(
                    name = LOCAL_ENVIRONMENT_NAME,
                    platforms = setOf(expectedPlatform(LOCAL_ENVIRONMENT_PROPERTY_VALUE)),
                ),
            )

        private fun expectedPlatform(value: String): Platform =
            Platform(
                platformType = PlatformType.JVM,
                properties = setOf(
                    Property(name = EXPECTED_PROPERTY_NAME, value = StringProperty(value)),
                )
            )

        private fun Project.extension(): Extension {
            pluginManager.apply(GradlePlugin::class.java)
            return extensions.findByType(Extension::class.java)!!
        }
    }
}