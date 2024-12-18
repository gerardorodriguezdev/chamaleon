package io.github.gerardorodriguezdev.chamaleon.core.testing.fakes

import io.github.gerardorodriguezdev.chamaleon.core.parsers.SchemaParser
import io.github.gerardorodriguezdev.chamaleon.core.parsers.SchemaParser.SchemaParserResult
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData
import java.io.File

class FakeSchemaParser(
    var schemaParserResult: SchemaParserResult = SchemaParserResult.Success(TestData.validCompleteSchema),
) : SchemaParser {
    override fun schemaParserResult(schemaFile: File): SchemaParserResult = schemaParserResult
}