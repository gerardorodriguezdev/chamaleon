package org.chamaleon.gradle.plugin

import org.chamaleon.core.EnvironmentsProcessor
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import java.util.*

class ChamaleonGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val extension = extensions.create(EXTENSION_NAME, ChamaleonExtension::class.java)

            val environmentsProcessor = EnvironmentsProcessor(environmentsDirectory.asFile)
            val environments = environmentsProcessor.environments()
            extension.environments.set(environments)

            val localProperties = localProperties()
            extension.selectedEnvironmentName.set(localProperties?.selectedEnvironmentName)
        }
    }

    private fun Project.localProperties(): LocalProperties? {
        val propertiesFile = environmentsDirectory.file(LOCAL_PROPERTIES_FILE).asFile
        if (!propertiesFile.exists()) return null

        val properties = Properties()
        properties.load(propertiesFile.inputStream())

        val selectedEnvironment = properties[SELECTED_ENVIRONMENT_KEY] as? String
        if (selectedEnvironment == null) return null

        return LocalProperties(
            selectedEnvironmentName = selectedEnvironment
        )
    }

    private val Project.environmentsDirectory: Directory
        get() = layout.projectDirectory.dir(ENVIRONMENTS_DIRECTORY)

    private data class LocalProperties(val selectedEnvironmentName: String)

    companion object {
        const val EXTENSION_NAME = "chamaleon"
        const val ENVIRONMENTS_DIRECTORY = "environments"
        const val LOCAL_PROPERTIES_FILE = "chamaleon.local.properties"
        const val SELECTED_ENVIRONMENT_KEY = "CHAMALEON_SELECTED_ENVIRONMENT"
    }
}