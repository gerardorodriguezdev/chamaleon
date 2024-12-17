package io.github.gerardorodriguezdev.chamaleon.core.testing.fakes

import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser
import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser.PropertiesParserResult

class FakePropertiesParser(
    var propertiesParserResult: PropertiesParserResult = PropertiesParserResult.Success(),
) : PropertiesParser {
    override fun propertiesParserResult(): PropertiesParserResult = propertiesParserResult
}