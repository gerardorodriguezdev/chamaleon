package org.chamaleon.core.testing.fakes

import org.chamaleon.core.parsers.EnvironmentsParser
import org.chamaleon.core.parsers.EnvironmentsParser.EnvironmentsParserResult
import org.chamaleon.core.testing.TestData

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