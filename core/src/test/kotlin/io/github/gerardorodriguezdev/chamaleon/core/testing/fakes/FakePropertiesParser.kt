package io.github.gerardorodriguezdev.chamaleon.core.testing.fakes

import io.github.gerardorodriguezdev.chamaleon.core.entities.results.AddOrUpdateSelectedEnvironmentResult
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.PropertiesParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData
import java.io.File

class FakePropertiesParser(
    var propertiesParserResult: PropertiesParserResult = PropertiesParserResult.Success(
        TestData.LOCAL_ENVIRONMENT_NAME,
    ),
    var addOrUpdateSelectedEnvironmentResult: AddOrUpdateSelectedEnvironmentResult =
        AddOrUpdateSelectedEnvironmentResult.Success,
) : PropertiesParser {
    override fun propertiesParserResult(propertiesFile: File): PropertiesParserResult = propertiesParserResult
    override fun addOrUpdateSelectedEnvironment(
        propertiesFile: File,
        newSelectedEnvironment: String?
    ): AddOrUpdateSelectedEnvironmentResult = addOrUpdateSelectedEnvironmentResult
}