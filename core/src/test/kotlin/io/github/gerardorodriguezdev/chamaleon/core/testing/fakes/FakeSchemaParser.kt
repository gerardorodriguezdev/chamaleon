package io.github.gerardorodriguezdev.chamaleon.core.testing.fakes

import io.github.gerardorodriguezdev.chamaleon.core.models.Schema
import io.github.gerardorodriguezdev.chamaleon.core.parsers.SchemaParser
import io.github.gerardorodriguezdev.chamaleon.core.results.AddSchemaResult
import io.github.gerardorodriguezdev.chamaleon.core.results.SchemaParserResult
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData
import java.io.File

class FakeSchemaParser(
    var schemaParserResult: SchemaParserResult = SchemaParserResult.Success(TestData.schema),
    var addSchemaResult: AddSchemaResult = AddSchemaResult.Success,
) : SchemaParser {
    override fun schemaParserResult(schemaFile: File): SchemaParserResult = schemaParserResult
    override fun addSchema(
        schemaFile: File,
        newSchema: Schema
    ): AddSchemaResult = addSchemaResult
}