package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.presentation.testing

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.EnvironmentsProcessorResult
import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import java.io.File

class FakeEnvironmentsProcessor(
    var processEnvironmentsProcessorResult: EnvironmentsProcessorResult = TestData.successEnvironmentsProcessorResult,
    var processRecursivelyResult: List<EnvironmentsProcessorResult> = listOf(
        TestData.successEnvironmentsProcessorResult
    ),
    var updateSelectedEnvironmentResult: Boolean = true,
    var addEnvironmentResult: Boolean = true,
) : EnvironmentsProcessor {

    override suspend fun process(environmentsDirectory: File): EnvironmentsProcessorResult =
        processEnvironmentsProcessorResult

    override suspend fun processRecursively(rootDirectory: File): List<EnvironmentsProcessorResult> =
        processRecursivelyResult

    override fun updateSelectedEnvironment(
        environmentsDirectory: File,
        newSelectedEnvironment: String?
    ): Boolean = updateSelectedEnvironmentResult

    override fun addEnvironments(
        environmentsDirectory: File,
        environments: Set<Environment>
    ): Boolean = addEnvironmentResult
}