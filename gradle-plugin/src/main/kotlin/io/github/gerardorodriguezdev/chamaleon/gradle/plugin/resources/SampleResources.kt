package io.github.gerardorodriguezdev.chamaleon.gradle.plugin.resources

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import java.io.File

internal object SampleResources {
    private const val SCHEMA_FILE_NAME = EnvironmentsProcessor.Companion.SCHEMA_FILE
    private val schemaFileContent = readSampleResource(SCHEMA_FILE_NAME)
    val schemaResource = Resource(SCHEMA_FILE_NAME, schemaFileContent)

    private const val PROPERTIES_FILE_NAME = EnvironmentsProcessor.Companion.PROPERTIES_FILE
    private val propertiesFileContent = readSampleResource(PROPERTIES_FILE_NAME)
    val propertiesResource = Resource(PROPERTIES_FILE_NAME, propertiesFileContent)

    private val localEnvironmentFileName = EnvironmentsProcessor.Companion.environmentFileName("local")
    private val localEnvironmentFileContent = readSampleResource(localEnvironmentFileName)
    val localEnvironmentResource = Resource(localEnvironmentFileName, localEnvironmentFileContent)

    private val productionEnvironmentFileName = EnvironmentsProcessor.Companion.environmentFileName("production")
    private val productionEnvironmentFileContent = readSampleResource(productionEnvironmentFileName)
    val productionEnvironmentResource = Resource(productionEnvironmentFileName, productionEnvironmentFileContent)

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