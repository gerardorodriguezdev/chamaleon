package io.github.gerardorodriguezdev.chamaleon.gradle.plugin

import io.github.gerardorodriguezdev.chamaleon.core.DefaultEnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.CONVENTION_ENVIRONMENTS_DIRECTORY_NAME
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory

class ChamaleonGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val extension = extensions.create(EXTENSION_NAME, ChamaleonExtension::class.java)

            val environmentsProcessor = DefaultEnvironmentsProcessor()
            val environmentsProcessorResult = environmentsProcessor.process(environmentsDirectory.asFile)

            extension.environments.set(environmentsProcessorResult.environments)
            extension.selectedEnvironmentName.set(environmentsProcessorResult.selectedEnvironmentName)
        }
    }

    private val Project.environmentsDirectory: Directory
        get() = layout.projectDirectory.dir(CONVENTION_ENVIRONMENTS_DIRECTORY_NAME)

    private companion object {
        const val EXTENSION_NAME = "chamaleon"
    }
}