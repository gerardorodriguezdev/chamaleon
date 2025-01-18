package io.github.gerardorodriguezdev.chamaleon.gradle.plugin

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import java.io.File

internal object SampleResources {
    private val schemaFileName = EnvironmentsProcessor.SCHEMA_FILE
    private val schemaFileContent = readSampleResource(schemaFileName)
    val schemaResource = Resource(schemaFileName, schemaFileContent)

    private val propertiesFileName = EnvironmentsProcessor.PROPERTIES_FILE
    private val propertiesFileContent = readSampleResource(propertiesFileName)
    val propertiesResource = Resource(propertiesFileName, propertiesFileContent)

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