package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.presentation.testing

import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectDeserializationResult
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectDeserializer
import java.io.File

class FakeProjectDeserializer(
    var processProjectDeserializationResult: ProjectDeserializationResult = TestData.successProjectDeserializationResult,
    var processRecursivelyResult: List<ProjectDeserializationResult> = listOf(
        TestData.successProjectDeserializationResult
    ),

    var addOrUpdateSelectedEnvironment: AddOrUpdateSelectedEnvironmentResult =
        AddOrUpdateSelectedEnvironmentResult.Success,

    var addEnvironmentResult: AddEnvironmentsResult = AddEnvironmentsResult.Success,
    var isEnvironmentValidResult: Boolean = true,

    var addSchemaResult: AddSchemaResult = AddSchemaResult.Success,
    var isSchemaValidResult: Boolean = true,
) : ProjectDeserializer {

    override suspend fun process(environmentsDirectory: File): ProjectDeserializationResult =
        processProjectDeserializationResult

    override suspend fun processRecursively(rootDirectory: File): List<ProjectDeserializationResult> =
        processRecursivelyResult

    override fun addOrUpdateSelectedEnvironment(
        environmentsDirectory: File,
        newSelectedEnvironment: String?
    ): AddOrUpdateSelectedEnvironmentResult = addOrUpdateSelectedEnvironment

    override fun addEnvironments(
        environmentsDirectory: File,
        environments: Set<Environment>
    ): AddEnvironmentsResult = addEnvironmentResult

    override fun isEnvironmentValid(environment: Environment): Boolean = isEnvironmentValidResult

    override fun addSchema(
        schemaFile: File,
        newSchema: Schema
    ): AddSchemaResult = addSchemaResult

    override fun isSchemaValid(schema: Schema): Boolean = isSchemaValidResult
}