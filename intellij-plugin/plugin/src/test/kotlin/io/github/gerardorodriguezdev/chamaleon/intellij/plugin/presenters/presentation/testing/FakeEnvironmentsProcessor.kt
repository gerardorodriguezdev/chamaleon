package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.presentation.testing

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.AddEnvironmentsResult
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.AddOrUpdateSelectedEnvironmentResult
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.AddSchemaResult
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.EnvironmentsProcessorResult
import java.io.File

class FakeEnvironmentsProcessor(
    var processEnvironmentsProcessorResult: EnvironmentsProcessorResult = TestData.successEnvironmentsProcessorResult,
    var processRecursivelyResult: List<EnvironmentsProcessorResult> = listOf(
        TestData.successEnvironmentsProcessorResult
    ),

    var addOrUpdateSelectedEnvironment: AddOrUpdateSelectedEnvironmentResult =
        AddOrUpdateSelectedEnvironmentResult.Success,

    var addEnvironmentResult: AddEnvironmentsResult = AddEnvironmentsResult.Success,
    var isEnvironmentValidResult: Boolean = true,

    var addSchemaResult: AddSchemaResult = AddSchemaResult.Success,
    var isSchemaValidResult: Boolean = true,
) : EnvironmentsProcessor {

    override suspend fun process(environmentsDirectory: File): EnvironmentsProcessorResult =
        processEnvironmentsProcessorResult

    override suspend fun processRecursively(rootDirectory: File): List<EnvironmentsProcessorResult> =
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