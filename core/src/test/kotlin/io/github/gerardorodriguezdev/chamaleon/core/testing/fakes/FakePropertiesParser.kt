package io.github.gerardorodriguezdev.chamaleon.core.testing.fakes

import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser
import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser.PropertiesParserResult
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData
import java.io.File

class FakePropertiesParser(
    var propertiesParserResult: PropertiesParserResult = PropertiesParserResult.Success(
        TestData.ENVIRONMENT_NAME,
    ),
) : PropertiesParser {
    override fun propertiesParserResult(propertiesFile: File): PropertiesParserResult = propertiesParserResult
}