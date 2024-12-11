package io.github.gerardorodriguezdev.chamaleon.gradle.plugin

import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform.Property
import io.github.gerardorodriguezdev.chamaleon.core.models.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.StringProperty
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.testing.TestData
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

class ChamaleonGradlePluginTest {
    @TempDir
    lateinit var testDir: File

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
        val buildFile = File(testDir, "build.gradle.kts")
        buildFile.writeText(buildFileContent)
    }

    private fun createFiles() {
        val environmentsDirectory = File(testDir, TestData.ENVIRONMENTS_DIRECTORY)
            .apply { mkdirs() }

        val templateJsonFile = File(environmentsDirectory, TestData.SCHEMA_FILE)
        templateJsonFile.writeText(templateJsonFileContent)

        val localJsonFile = File(environmentsDirectory, TestData.LOCAL_ENVIRONMENT_FILE)
        localJsonFile.writeText(localJsonFileContent)

        val productionJsonFile = File(environmentsDirectory, TestData.PRODUCTION_ENVIRONMENT_FILE)
        productionJsonFile.writeText(productionJsonFileContent)

        val localPropertiesFile = File(environmentsDirectory, TestData.LOCAL_PROPERTIES_FILE)
        localPropertiesFile.writeText(localPropertiesFileContent)
    }

    private fun buildResult(): BuildResult =
        GradleRunner
            .create()
            .withProjectDir(testDir)
            .withPluginClasspath()
            .withArguments("help")
            .build()

    private fun buildProject(configuration: Project.() -> Unit = {}): Project =
        ProjectBuilder
            .builder()
            .withProjectDir(testDir)
            .build()
            .apply(configuration)

    private fun Project.extension(): ChamaleonExtension {
        pluginManager.apply(ChamaleonGradlePlugin::class.java)
        return extensions.findByType(ChamaleonExtension::class.java)!!
    }

    private companion object {
        const val EXPECTED_PROPERTY_NAME = "HOST"

        const val LOCAL_ENVIRONMENT_NAME = "local-cha"
        const val LOCAL_ENVIRONMENT_HOST = "localhost"

        const val PRODUCTION_ENVIRONMENT_NAME = "production-cha"
        const val PRODUCTION_ENVIRONMENT_HOST = "otherhost"

        val buildFileContent =
            //language=kotlin
            """
                plugins {
                    id("io.github.gerardorodriguezdev.chamaleon")
                }
            """.trimIndent()

        val templateJsonFileContent =
            //language=json
            """
                {
                  "supportedPlatforms": [
                    "android"
                  ],
                  "propertyDefinitions": [
                    {
                      "name": "HOST",
                      "propertyType": "String",
                      "nullable": false
                    }
                  ]
                }
            """.trimIndent()

        val localJsonFileContent =
            //language=json
            """
                [
                  {
                    "platformType": "android",
                    "properties": [
                      {
                        "name": "HOST",
                        "value": "localhost"
                      }
                    ]
                  }
                ]
            """.trimIndent()

        val productionJsonFileContent =
            //language=json
            """
                [
                  {
                    "platformType": "android",
                    "properties": [
                      {
                        "name": "HOST",
                        "value": "otherhost"
                      }
                    ]
                  }
                ]
            """.trimIndent()

        val localPropertiesFileContent =
            //language=properties
            """
                CHAMALEON_SELECTED_ENVIRONMENT=$LOCAL_ENVIRONMENT_NAME
            """.trimIndent()

        val expectedEnvironments =
            setOf(
                Environment(
                    name = LOCAL_ENVIRONMENT_NAME,
                    platforms = setOf(expectedPlatform(LOCAL_ENVIRONMENT_HOST)),
                ),
                Environment(
                    name = PRODUCTION_ENVIRONMENT_NAME,
                    platforms = setOf(expectedPlatform(PRODUCTION_ENVIRONMENT_HOST)),
                ),
            )

        private fun expectedPlatform(value: String): Platform =
            Platform(
                platformType = PlatformType.android,
                properties = setOf(
                    Property(name = EXPECTED_PROPERTY_NAME, value = StringProperty(value)),
                )
            )
    }
}