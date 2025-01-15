package io.github.gerardorodriguezdev.chamaleon.core.testing.fakes

import io.github.gerardorodriguezdev.chamaleon.core.parsers.EnvironmentsParser
import io.github.gerardorodriguezdev.chamaleon.core.parsers.EnvironmentsParser.EnvironmentsParserResult
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData
import java.io.File

class FakeEnvironmentsParser(
    var fakeRestrictedFileNames: List<String> = emptyList(),
    var environmentsParserResult: EnvironmentsParserResult =
        EnvironmentsParserResult.Success(
            environments = setOf(
                TestData.validCompleteEnvironment
            )
        ),
) : EnvironmentsParser {
    override val restrictedFileNames: List<String> = fakeRestrictedFileNames

    override fun environmentsParserResult(environmentsDirectory: File): EnvironmentsParserResult =
        environmentsParserResult
}