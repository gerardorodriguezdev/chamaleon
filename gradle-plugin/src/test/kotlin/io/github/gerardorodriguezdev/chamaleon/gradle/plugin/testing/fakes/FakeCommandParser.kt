package io.github.gerardorodriguezdev.chamaleon.gradle.plugin.testing.fakes

import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.CommandParser
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.CommandParser.CommandParserResult

internal class FakeCommandParser(
    var commandParserResults: List<CommandParserResult>,
) : CommandParser {
    var currentResult = 0

    override fun parse(commands: List<String>): CommandParserResult = commandParserResults[currentResult++]
}