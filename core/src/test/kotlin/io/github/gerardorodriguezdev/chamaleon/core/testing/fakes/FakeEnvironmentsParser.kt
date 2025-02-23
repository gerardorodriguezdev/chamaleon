package io.github.gerardorodriguezdev.chamaleon.core.testing.fakes

import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.parsers.EnvironmentsParser
import io.github.gerardorodriguezdev.chamaleon.core.results.AddEnvironmentsResult
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsParserResult
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
) : EnvironmentsParser {
    override fun environmentsParserResult(environmentsDirectory: File): EnvironmentsParserResult =
        environmentsParserResult

    override fun addEnvironments(
        environmentsDirectory: File,
        newEnvironments: Set<Environment>
    ): AddEnvironmentsResult = addEnvironmentsResult
}