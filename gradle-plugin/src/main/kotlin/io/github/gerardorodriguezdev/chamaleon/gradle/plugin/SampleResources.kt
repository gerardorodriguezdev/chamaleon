package io.github.gerardorodriguezdev.chamaleon.gradle.plugin

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import java.io.File

internal object SampleResources {
    private const val SCHEMA_FILE_NAME = EnvironmentsProcessor.SCHEMA_FILE
    private val schemaFileContent = readSampleResource(SCHEMA_FILE_NAME)
    val schemaResource = Resource(SCHEMA_FILE_NAME, schemaFileContent)

    private const val PROPERTIES_FILE_NAME = EnvironmentsProcessor.PROPERTIES_FILE
    private val propertiesFileContent = readSampleResource(PROPERTIES_FILE_NAME)
    val propertiesResource = Resource(PROPERTIES_FILE_NAME, propertiesFileContent)

    private val localEnvironmentFileName = EnvironmentsProcessor.environmentFileName("local")
    private val localEnvironmentFileContent = readSampleResource(localEnvironmentFileName)
    val localEnvironmentResource = Resource(localEnvironmentFileName, localEnvironmentFileContent)

    private val productionEnvironmentFileName = EnvironmentsProcessor.environmentFileName("production")
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
            File(environmentsDirectory, fileName).writeText(fileContent)
        }
    }
}