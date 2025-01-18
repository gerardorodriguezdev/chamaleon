package io.github.gerardorodriguezdev.chamaleon.gradle.plugin

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
public abstract class InitTask : DefaultTask() {
    private val fileNames = listOf(
        EnvironmentsProcessor.SCHEMA_FILE,
        EnvironmentsProcessor.PROPERTIES_FILE,
        EnvironmentsProcessor.environmentFileName("local"),
        EnvironmentsProcessor.environmentFileName("production"),
    )

    @get:OutputDirectory
    public abstract val environmentsDirectory: DirectoryProperty

    @TaskAction
    public fun generateSampleConfigurationFiles() {
        val environmentsDirectory = environmentsDirectory.get().asFile

        fileNames.forEach { fileName ->
            val file = File(environmentsDirectory, fileName)
            val fileContent = readResource(fileName)
            file.writeText(fileContent)
        }
    }

    private fun readResource(fileName: String): String = object {}.javaClass.getResource("/$fileName")?.readText()!!
}