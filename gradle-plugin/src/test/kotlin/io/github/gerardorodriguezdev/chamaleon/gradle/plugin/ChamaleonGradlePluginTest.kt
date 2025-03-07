package io.github.gerardorodriguezdev.chamaleon.gradle.plugin

import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform.Property
import io.github.gerardorodriguezdev.chamaleon.core.models.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.StringProperty
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore.Companion.toUnsafeNonEmptyKeyStore
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString.Companion.toUnsafeNonEmptyString
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.ChamaleonGradlePlugin.Companion.GENERATE_ENVIRONMENT_COMMAND_LINE_ARGUMENT
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.ChamaleonGradlePlugin.Companion.GENERATE_ENVIRONMENT_TASK_NAME
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.ChamaleonGradlePlugin.Companion.GENERATE_SAMPLE_TASK_NAME
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.ChamaleonGradlePlugin.Companion.SELECT_ENVIRONMENT_TASK_NAME
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.extensions.ChamaleonExtension
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.resources.SampleResources
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.resources.SampleResources.writeAll
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.CommandParser
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
import io.github.gerardorodriguezdev.chamaleon.core.models.Project as ChamaleonProject

//TODO: Refactor?
class ChamaleonGradlePluginTest {
    @TempDir
    lateinit var directory: File

    @BeforeEach
    fun setUp() {
        createBuildFiles()
    }

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
        val extensionProject = extension.project.get()
        assertEquals(expected = expectedEnvironments, actual = extensionProject.environments)
        assertEquals(
            expected = LOCAL_ENVIRONMENT_NAME,
            actual = extensionProject.selectedEnvironment()?.name?.value,
        )
    }

    @Test
    fun `GIVEN plugin is applied WHEN generateSampleTask is executed THEN generates sample files`() {
        val buildResult = generateSampleTaskBuildResult()

        assertEquals(
            expected = TaskOutcome.SUCCESS,
            actual = buildResult.outcomeOfTask(GENERATE_SAMPLE_TASK_NAME)
        )
        val environmentsDirectory = environmentsDirectory()
        val environmentsFiles = environmentsDirectory.listFiles()
        assertEquals(expected = environmentsFiles.size, actual = SampleResources.resources.size)
        environmentsFiles.forEach { environmentFile ->
            val resource =
                SampleResources.resources.firstOrNull { resource -> resource.fileName == environmentFile.name }
            assertEquals(expected = resource?.fileContent, actual = environmentFile.readText())
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

        val localEnvironmentFile =
            environmentsFiles.firstOrNull { environmentFile ->
                environmentFile.name == SampleResources.localEnvironmentResource.fileName
            }
        val localEnvironmentFileContent = localEnvironmentFile?.readText()
        assertEquals(
            expected = SampleResources.localEnvironmentResource.fileContent,
            actual = localEnvironmentFileContent,
        )
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
        val propertiesFile =
            environmentsFiles.firstOrNull { environmentFile -> environmentFile.name == ChamaleonProject.PROPERTIES_FILE }
        val propertiesFileContent = propertiesFile?.readText()
        assertEquals(
            expected = propertiesFileContentWithProductionEnvironmentSelected,
            actual = propertiesFileContent,
        )
    }

    private fun createBuildFiles() {
        val buildFile = File(directory, BUILD_FILE_NAME)
        buildFile.writeText(buildFileContent)
    }

    private fun createSchemaFile() {
        val environmentsDirectory = environmentsDirectory()
        environmentsDirectory.mkdir()
        SampleResources.schemaResource.writeContent(environmentsDirectory)
    }

    private fun createEnvironmentsFiles() {
        val environmentsDirectory = environmentsDirectory()
        environmentsDirectory.mkdir()
        SampleResources.resources.writeAll(environmentsDirectory)
    }

    private fun environmentsDirectory(): File = File(directory, ChamaleonProject.ENVIRONMENTS_DIRECTORY_NAME)

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
        const val LOCAL_ENVIRONMENT_NAME = "local"
        const val LOCAL_ENVIRONMENT_PROPERTY_VALUE = "YourPropertyValueForLocalEnvironment"

        const val PRODUCTION_ENVIRONMENT_NAME = "production"
        const val PRODUCTION_ENVIRONMENT_PROPERTY_VALUE = "YourPropertyValueForProductionEnvironment"

        const val PROPERTY_NAME = "YourPropertyName"

        const val HELP_TASK_NAME = "help"

        const val BUILD_FILE_NAME = "build.gradle.kts"
        val buildFileContent =
            //language=kotlin
            """
                plugins {
                    id("io.github.gerardorodriguezdev.chamaleon")
                }
            """.trimIndent()

        val propertiesFileContentWithProductionEnvironmentSelected =
            //language=JSON
            """
                {
                  "selectedEnvironmentName": "production"
                }
            """.trimIndent()

        val expectedEnvironments =
            setOf(
                Environment(
                    name = PRODUCTION_ENVIRONMENT_NAME.toUnsafeNonEmptyString(),
                    platforms =
                        setOf(
                            expectedPlatform(
                                PRODUCTION_ENVIRONMENT_PROPERTY_VALUE
                            )
                        ).toUnsafeNonEmptyKeyStore(),
                ),
                Environment(
                    name = LOCAL_ENVIRONMENT_NAME.toUnsafeNonEmptyString(),
                    platforms =
                        setOf(
                            expectedPlatform(
                                LOCAL_ENVIRONMENT_PROPERTY_VALUE
                            )
                        ).toUnsafeNonEmptyKeyStore(),
                ),
            ).toUnsafeNonEmptyKeyStore()

        private fun expectedPlatform(value: String): Platform =
            Platform(
                platformType = PlatformType.JVM,
                properties = setOf(
                    Property(
                        name = PROPERTY_NAME.toUnsafeNonEmptyString(),
                        value = StringProperty(value.toUnsafeNonEmptyString())
                    ),
                ).toUnsafeNonEmptyKeyStore()
            )

        private fun Project.extension(): ChamaleonExtension {
            pluginManager.apply(ChamaleonGradlePlugin::class.java)
            return extensions.findByType(ChamaleonExtension::class.java)!!
        }

        private fun BuildResult.outcomeOfTask(taskName: String): TaskOutcome? = task(taskPath(taskName))?.outcome

        private fun commandLineArgument(key: String, value: String): String = "-P$key=$value"

        private fun taskPath(taskName: String): String = ":$taskName"
    }
}