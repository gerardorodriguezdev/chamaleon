package io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

//TODO: Add merging of all environments created by commands
//TODO: Maybe enable adding single prop per string?
@CacheableTask
public abstract class GenerateEnvironmentTask : DefaultTask() {

    @get:Input
    public abstract val generateEnvironmentCommands: Property<List<String>>

    @get:OutputDirectory
    public abstract val environmentsDirectory: DirectoryProperty

    @TaskAction
    public fun generateEnvironment() {
    }
}