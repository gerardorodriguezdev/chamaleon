package io.github.gerardorodriguezdev.chamaleon.core.testing.fakes

import io.github.gerardorodriguezdev.chamaleon.core.parsers.EnvironmentsParser
import io.github.gerardorodriguezdev.chamaleon.core.parsers.EnvironmentsParser.EnvironmentsParserResult
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData

class FakeEnvironmentsParser(
    var environmentsParserResult: EnvironmentsParserResult =
        EnvironmentsParserResult.Success(
            environments = setOf(
                TestData.validCompleteEnvironment
            )
        ),
) : EnvironmentsParser {
    override fun environmentsParserResult(): EnvironmentsParserResult = environmentsParserResult
}