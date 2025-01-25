package io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks

import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.resources.SampleResources
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.resources.SampleResources.writeAll
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

@CacheableTask
public abstract class GenerateSampleTask : DefaultTask() {
    @get:OutputDirectory
    public abstract val environmentsDirectory: DirectoryProperty

    @TaskAction
    public fun generateSample() {
        val environmentsDirectory = environmentsDirectory.get().asFile
        SampleResources.resources.writeAll(environmentsDirectory)
    }
}