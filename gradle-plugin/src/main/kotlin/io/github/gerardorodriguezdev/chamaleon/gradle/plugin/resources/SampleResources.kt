package io.github.gerardorodriguezdev.chamaleon.gradle.plugin.resources

import io.github.gerardorodriguezdev.chamaleon.core.models.Project
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString.Companion.toUnsafeNonEmptyString
import java.io.File

//TODO: Replace
internal object SampleResources {
    private const val SCHEMA_FILE_NAME = Project.SCHEMA_FILE
    private val schemaFileContent = readSampleResource(SCHEMA_FILE_NAME)
    val schemaResource = Resource(SCHEMA_FILE_NAME, schemaFileContent)

    private const val PROPERTIES_FILE_NAME = Project.PROPERTIES_FILE
    private val propertiesFileContent = readSampleResource(PROPERTIES_FILE_NAME)
    val propertiesResource = Resource(PROPERTIES_FILE_NAME, propertiesFileContent)

    private const val LOCAL_ENVIRONMENT_NAME = "local"
    private val localEnvironmentFileName =
        Project.environmentFileName(LOCAL_ENVIRONMENT_NAME.toUnsafeNonEmptyString())
    private val localEnvironmentFileContent = readSampleResource(localEnvironmentFileName.value)
    val localEnvironmentResource = Resource(localEnvironmentFileName.value, localEnvironmentFileContent)

    private const val PRODUCTION_ENVIRONMENT_NAME = "production"
    private val productionEnvironmentFileName =
        Project.environmentFileName(PRODUCTION_ENVIRONMENT_NAME.toUnsafeNonEmptyString())
    private val productionEnvironmentFileContent = readSampleResource(productionEnvironmentFileName.value)
    val productionEnvironmentResource = Resource(productionEnvironmentFileName.value, productionEnvironmentFileContent)

    val resources = listOf(
        schemaResource,
        propertiesResource,
        localEnvironmentResource,
        productionEnvironmentResource,
    )

    private fun readSampleResource(fileName: String): String =
        object {}.javaClass.getResource("/$fileName")?.readText()!!

    fun List<Resource>.writeAll(environmentsDirectory: File) {
        forEach { resource -> resource.writeContent(environmentsDirectory) }
    }

    data class Resource(
        val fileName: String,
        val fileContent: String,
    ) {
        fun writeContent(environmentsDirectory: File) {
            val file = File(environmentsDirectory, fileName)
            if (file.exists()) return
            file.writeText(fileContent)
        }
    }
}