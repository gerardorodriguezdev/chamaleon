package org.chamaleon.core.testing.fakes

import org.chamaleon.core.parsers.SchemaParser
import org.chamaleon.core.parsers.SchemaParser.SchemaParserResult
import org.chamaleon.core.testing.TestData

class FakeSchemaParser(
    var schemaParserResult: SchemaParserResult = SchemaParserResult.Success(TestData.validCompleteSchema),
) : SchemaParser {
    override fun schemaParserResult(): SchemaParserResult = schemaParserResult
}