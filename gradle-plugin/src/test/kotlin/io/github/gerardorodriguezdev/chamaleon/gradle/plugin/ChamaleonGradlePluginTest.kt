package io.github.gerardorodriguezdev.chamaleon.gradle.plugin

import io.github.gerardorodriguezdev.chamaleon.core.models.Platform.Property
import io.github.gerardorodriguezdev.chamaleon.core.models.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.models.Project.Companion.projectOf
import io.github.gerardorodriguezdev.chamaleon.core.models.Properties
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.StringProperty
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory.Companion.toUnsafeExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString.Companion.toUnsafeNonEmptyString
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectSerializer
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.ChamaleonGradlePlugin.Companion.GENERATE_ENVIRONMENT_COMMAND_LINE_ARGUMENT
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.ChamaleonGradlePlugin.Companion.GENERATE_ENVIRONMENT_TASK_NAME
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.ChamaleonGradlePlugin.Companion.GENERATE_SAMPLE_TASK_NAME
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.ChamaleonGradlePlugin.Companion.SELECT_ENVIRONMENT_TASK_NAME
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.extensions.ChamaleonExtension
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.GenerateSampleTask
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.GenerateSampleTask.Companion.LOCAL_ENVIRONMENT_NAME
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.GenerateSampleTask.Companion.LOCAL_ENVIRONMENT_PROPERTY_VALUE
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.GenerateSampleTask.Companion.PRODUCTION_ENVIRONMENT_NAME
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.GenerateSampleTask.Companion.PROPERTY_NAME
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.GenerateSampleTask.Companion.sampleProject
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.CommandParser
import kotlinx.coroutines.runBlocking
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import io.github.gerardorodriguezdev.chamaleon.core.models.Project as ChamaleonProject

class ChamaleonGradlePluginTest {
    val projectSerializer = ProjectSerializer.create()

    @TempDir
    lateinit var directory: File

    @BeforeEach
    fun setUp() {
        createBuildFile()
    }

    @Nested
    inner class ScanProject {

        @Test
        fun `GIVEN plugin is applied WHEN task is executed THEN build is successful`() {
            createEnvironmentsFiles()

            val buildResult = helpTaskBuildResult()

            assertEquals(expected = TaskOutcome.SUCCESS, actual = buildResult.outcomeOfTask(HELP_TASK_NAME))
        }

        @Test
        fun `GIVEN plugin is applied WHEN project is configured THEN valid extension is returned`() {
            createEnvironmentsFiles()

            val project = buildProject()

            val extension = project.extension()
            assertEquals(
                expected = GenerateSampleTask.sampleLocalEnvironment,
                actual = extension.environment(LOCAL_ENVIRONMENT_NAME)
            )
            assertEquals(
                expected = GenerateSampleTask.sampleLocalEnvironment,
                actual = extension.selectedEnvironment(),
            )
        }
    }

    @Nested
    inner class TasksExecution {

        @Test
        fun `GIVEN plugin is applied WHEN generateSampleTask is executed THEN generates sample files`() {
            val environmentsDirectory = environmentsDirectory(createIfNotPresent = false)
            val buildResult = generateSampleTaskBuildResult()

            assertEquals(
                expected = TaskOutcome.SUCCESS,
                actual = buildResult.outcomeOfTask(GENERATE_SAMPLE_TASK_NAME)
            )

            val environmentsFiles = environmentsDirectory.listFiles()
            assertEquals(expected = environmentsFiles.size, actual = 4)
            assertTrue {
                environmentsFiles.containsValidFiles(
                    LOCAL_ENVIRONMENT_FILE_NAME,
                    PRODUCTION_ENVIRONMENT_FILE_NAME,
                    ChamaleonProject.SCHEMA_FILE,
                    ChamaleonProject.PROPERTIES_FILE,
                )
            }
        }

        @Test
        fun `GIVEN plugin is applied WHEN generateEnvironmentTask is executed THEN generates environment files`() {
            createSchemaFile()

            val buildResult = generateEnvironmentTaskBuildResult()

            assertEquals(
                expected = TaskOutcome.SUCCESS,
                actual = buildResult.outcomeOfTask(GENERATE_ENVIRONMENT_TASK_NAME)
            )

            val environmentsDirectory = environmentsDirectory()
            val environmentsFiles = environmentsDirectory.listFiles()
            assertEquals(expected = environmentsFiles.size, actual = 3)
            assertTrue {
                environmentsFiles.containsValidFiles(
                    LOCAL_ENVIRONMENT_FILE_NAME,
                    ChamaleonProject.SCHEMA_FILE,
                    ChamaleonProject.PROPERTIES_FILE,
                )
            }
        }

        @Test
        fun `GIVEN plugin is applied WHEN selectEnvironment is executed THEN selects new environment`() {
            createEnvironmentsFiles()

            val buildResult = selectEnvironmentTaskBuildResult()

            assertEquals(
                expected = TaskOutcome.SUCCESS,
                actual = buildResult.outcomeOfTask(SELECT_ENVIRONMENT_TASK_NAME),
            )

            val environmentsDirectory = environmentsDirectory()
            val environmentsFiles = environmentsDirectory.listFiles()
            assertEquals(expected = environmentsFiles.size, actual = 4)

            val propertiesFile = environmentsFiles.propertiesFile()
            val propertiesFileContent = propertiesFile?.readText()
            assertEquals(
                expected = newPropertiesFileContent,
                actual = propertiesFileContent,
            )
        }
    }

    private fun createBuildFile() {
        val buildFile = File(directory, BUILD_FILE_NAME)
        buildFile.writeText(buildFileContent)
    }

    private fun createEnvironmentsFiles() {
        runBlocking {
            val existingEnvironmentsDirectory = environmentsDirectory().toUnsafeExistingDirectory()
            val sampleProject = sampleProject(existingEnvironmentsDirectory)
            projectSerializer.serialize(sampleProject)
        }
    }

    private fun createSchemaFile() {
        runBlocking {
            val environmentsDirectory = environmentsDirectory().toUnsafeExistingDirectory()
            val project = requireNotNull(
                projectOf(
                    environmentsDirectory = environmentsDirectory,
                    schema = GenerateSampleTask.sampleSchema,
                    properties = Properties(),
                    environments = null,
                ).project()
            )
            projectSerializer.serialize(project)
        }
    }

    private fun environmentsDirectory(createIfNotPresent: Boolean = true): File =
        File(directory, ChamaleonProject.ENVIRONMENTS_DIRECTORY_NAME).apply {
            if (createIfNotPresent) {
                if (!exists()) {
                    mkdirs()
                }
            }
        }

    private fun helpTaskBuildResult(): BuildResult =
        GradleRunner
            .create()
            .withProjectDir(directory)
            .withPluginClasspath()
            .withArguments(HELP_TASK_NAME)
            .build()

    private fun generateSampleTaskBuildResult(): BuildResult =
        GradleRunner
            .create()
            .withProjectDir(directory)
            .withPluginClasspath()
            .withArguments(GENERATE_SAMPLE_TASK_NAME)
            .build()

    private fun generateEnvironmentTaskBuildResult(): BuildResult =
        GradleRunner
            .create()
            .withProjectDir(directory)
            .withPluginClasspath()
            .withArguments(
                listOf(
                    GENERATE_ENVIRONMENT_TASK_NAME,
                    commandLineArgument(
                        key = GENERATE_ENVIRONMENT_COMMAND_LINE_ARGUMENT,
                        value = CommandParser.generateCommand(
                            environmentName = LOCAL_ENVIRONMENT_NAME,
                            platformType = PlatformType.JVM,
                            properties = listOf(
                                Property(
                                    name = PROPERTY_NAME.toUnsafeNonEmptyString(),
                                    value = StringProperty(LOCAL_ENVIRONMENT_PROPERTY_VALUE.toUnsafeNonEmptyString())
                                )
                            )
                        )
                    )
                ),
            )
            .build()

    private fun selectEnvironmentTaskBuildResult(): BuildResult =
        GradleRunner
            .create()
            .withProjectDir(directory)
            .withPluginClasspath()
            .withArguments(
                listOf(
                    SELECT_ENVIRONMENT_TASK_NAME,
                    commandLineArgument(
                        key = ChamaleonGradlePlugin.SELECT_ENVIRONMENT_COMMAND_LINE_ARGUMENT,
                        value = PRODUCTION_ENVIRONMENT_NAME,
                    )
                )
            )
            .build()

    private fun buildProject(): Project =
        ProjectBuilder
            .builder()
            .withProjectDir(directory)
            .build()

    private companion object {
        const val HELP_TASK_NAME = "help"

        const val BUILD_FILE_NAME = "build.gradle.kts"

        val LOCAL_ENVIRONMENT_FILE_NAME = ChamaleonProject.environmentFileName(
            LOCAL_ENVIRONMENT_NAME.toUnsafeNonEmptyString()
        ).value

        val PRODUCTION_ENVIRONMENT_FILE_NAME = ChamaleonProject.environmentFileName(
            PRODUCTION_ENVIRONMENT_NAME.toUnsafeNonEmptyString()
        ).value

        val buildFileContent =
            //language=kotlin
            """
                plugins {
                    id("io.github.gerardorodriguezdev.chamaleon")
                }
            """.trimIndent()

        val newPropertiesFileContent =
            //language=json
            """
                {
                  "selectedEnvironmentName": "production"
                }
            """.trimIndent()

        private fun Project.extension(): ChamaleonExtension {
            pluginManager.apply(ChamaleonGradlePlugin::class.java)
            return requireNotNull(extensions.findByType(ChamaleonExtension::class.java))
        }

        private fun BuildResult.outcomeOfTask(taskName: String): TaskOutcome? = task(taskPath(taskName))?.outcome

        private fun commandLineArgument(key: String, value: String): String = "-P$key=$value"

        private fun taskPath(taskName: String): String = ":$taskName"

        private fun Array<File>.containsValidFiles(vararg fileNames: String): Boolean {
            fileNames.forEach { fileName ->
                any { file -> file.isValid(fileName) }
            }
            return true
        }

        private fun Array<File>.propertiesFile(): File? = firstOrNull { environmentFile ->
            environmentFile.name == ChamaleonProject.PROPERTIES_FILE
        }

        private fun File.isValid(fileName: String): Boolean = name == fileName && readText().isNotEmpty()
    }
}