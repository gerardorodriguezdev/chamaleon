package io.github.gerardorodriguezdev.chamaleon.core.testing.fakes

import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.AddSchemaResult
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.SchemaParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.SchemaParser
import io.github.gerardorodriguezdev.chamaleon.core.testing.TestData
import java.io.File

class FakeSchemaParser(
    var schemaParserResult: SchemaParserResult = SchemaParserResult.Success(TestData.schema),
    var addSchemaResult: AddSchemaResult = AddSchemaResult.Success,
    var isSchemaValidResult: Boolean = true
) : SchemaParser {
    override fun schemaParserResult(schemaFile: File): SchemaParserResult = schemaParserResult
    override fun addSchema(
        schemaFile: File,
        schema: Schema
    ): AddSchemaResult = addSchemaResult

    override fun isSchemaValid(schema: Schema): Boolean = isSchemaValidResult
}