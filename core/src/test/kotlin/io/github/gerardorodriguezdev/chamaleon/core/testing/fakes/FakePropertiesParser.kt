package io.github.gerardorodriguezdev.chamaleon.core.testing.fakes

import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser
import io.github.gerardorodriguezdev.chamaleon.core.results.AddOrUpdateSelectedEnvironmentResult
import io.github.gerardorodriguezdev.chamaleon.core.results.PropertiesParserResult
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData
import java.io.File

class FakePropertiesParser(
    var propertiesParserResult: PropertiesParserResult = PropertiesParserResult.Success(
        TestData.LOCAL_ENVIRONMENT_NAME,
    ),
    var addOrUpdateSelectedEnvironmentResult: AddOrUpdateSelectedEnvironmentResult =
        AddOrUpdateSelectedEnvironmentResult.Success,
) : PropertiesParser {
    override fun parse(propertiesFile: File): PropertiesParserResult = propertiesParserResult
    override fun addOrUpdateSelectedEnvironment(
        propertiesFile: File,
        newSelectedEnvironment: String?
    ): AddOrUpdateSelectedEnvironmentResult = addOrUpdateSelectedEnvironmentResult
}