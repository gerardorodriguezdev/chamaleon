package io.github.gerardorodriguezdev.chamaleon.core.testing.fakes

import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.AddEnvironmentsResult
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.EnvironmentsParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.EnvironmentsParser
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData
import java.io.File

class FakeEnvironmentsParser(
    var environmentsParserResult: EnvironmentsParserResult =
        EnvironmentsParserResult.Success(
            environments = setOf(
                TestData.environment
            )
        ),
    var addEnvironmentsResult: AddEnvironmentsResult = AddEnvironmentsResult.Success,
    var isEnvironmentValid: Boolean = true
) : EnvironmentsParser {
    override fun environmentsParserResult(environmentsDirectory: File): EnvironmentsParserResult =
        environmentsParserResult

    override fun addEnvironments(
        environmentsDirectory: File,
        environments: Set<Environment>
    ): AddEnvironmentsResult = addEnvironmentsResult

    override fun isEnvironmentValid(environment: Environment): Boolean = isEnvironmentValid
}