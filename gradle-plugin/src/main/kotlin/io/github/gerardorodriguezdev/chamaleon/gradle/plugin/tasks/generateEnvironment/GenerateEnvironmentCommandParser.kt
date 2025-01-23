package io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment

import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Platform
import io.github.gerardorodriguezdev.chamaleon.core.entities.Platform.Property
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyValue
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.GenerateEnvironmentCommandParser.GenerateEnvironmentCommandParserResult.Failure
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.GenerateEnvironmentCommandParser.GenerateEnvironmentCommandParserResult.Success

internal class GenerateEnvironmentCommandParser {

    fun parse(command: String): GenerateEnvironmentCommandParserResult? {
        val regexResult = regex.find(command) ?: return Failure.InvalidCommand(command)

        val (environmentNameString, platformTypeString, propertiesString) = regexResult.destructured

        if (environmentNameString.isEmpty()) return Failure.InvalidCommand(command)

        val platformType = platformTypeString.toPlatformType() ?: return Failure.InvalidPlatformType(platformTypeString)
        val properties = propertiesString.toProperties()

        if (properties.isEmpty()) return Failure.InvalidCommand(command)

        return Success(
            environment = Environment(
                name = environmentNameString,
                platforms = setOf(
                    Platform(
                        platformType = platformType,
                        properties = properties,
                    )
                )
            )
        )
    }

    private fun String.toPlatformType(): PlatformType? =
        PlatformType.values().firstOrNull { platformType -> platformType.serialName == this }

    private fun String.toProperties(): Set<Property> =
        this
            .propertyStringPairs()
            .filter { propertyString -> propertyString.isNotEmpty() }
            .map { propertyString -> propertyString.toProperty() }
            .toSet()

    private fun String.propertyStringPairs(): List<String> = split(",")

    private fun String.toProperty(): Property {
        val (propertyNameString, propertyValueString) = split("=")
        val propertyValue = propertyValueString.toPropertyValue()
        return Property(name = propertyNameString, value = propertyValue)
    }

    private fun String.toPropertyValue(): PropertyValue {
        val booleanValue = toBooleanStrictOrNull()
        return if (booleanValue != null) {
            PropertyValue.BooleanProperty(booleanValue)
        } else {
            PropertyValue.StringProperty(this)
        }
    }

    sealed interface GenerateEnvironmentCommandParserResult {
        data class Success(val environment: Environment) : GenerateEnvironmentCommandParserResult

        sealed interface Failure : GenerateEnvironmentCommandParserResult {
            data class InvalidCommand(val command: String) : Failure
            data class InvalidPlatformType(val platformTypeString: String) : Failure
        }
    }

    companion object {
        val regex =
            //language=regexp
            """
            chamaleonEnvironment="(.*?)\.(.*?)\.properties\[(.*?)]"
            """.trimIndent().toRegex()
    }
}