package io.github.gerardorodriguezdev.chamaleon.gradle.plugin

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Platform
import io.github.gerardorodriguezdev.chamaleon.core.entities.Platform.Property
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyValue.StringProperty
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
        createFiles()
    }

    @Test
    fun `GIVEN plugin is applied to project WHEN task is executed THEN build is successful`() {
        val buildResult = buildResult()

        assertEquals(expected = TaskOutcome.SUCCESS, actual = buildResult.task(":help")?.outcome)
    }

    @Test
    fun `GIVEN plugin is applied to project WHEN project is configured THEN valid extension is returned`() {
        val project = buildProject()
        val extension = project.extension()

        assertEquals(expected = expectedEnvironments, actual = extension.environments.get())
        assertEquals(
            expected = LOCAL_ENVIRONMENT_NAME,
            actual = extension.selectedEnvironmentName.get()
        )
    }

    private fun createBuildFiles() {
        val buildFile = File(directory, BUILD_FILE_NAME)
        buildFile.writeText(buildFileContent)
    }

    private fun createFiles() {
        val environmentsDirectory =
            File(directory, EnvironmentsProcessor.ENVIRONMENTS_DIRECTORY_NAME).apply { mkdirs() }
        SampleResources.resources.writeAll(environmentsDirectory)
    }

    private fun buildResult(): BuildResult =
        GradleRunner
            .create()
            .withProjectDir(directory)
            .withPluginClasspath()
            .withArguments("help")
            .build()

    private fun buildProject(configuration: Project.() -> Unit = {}): Project =
        ProjectBuilder
            .builder()
            .withProjectDir(directory)
            .build()
            .apply(configuration)

    private fun Project.extension(): Extension {
        pluginManager.apply(GradlePlugin::class.java)
        return extensions.findByType(Extension::class.java)!!
    }

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
    }
}