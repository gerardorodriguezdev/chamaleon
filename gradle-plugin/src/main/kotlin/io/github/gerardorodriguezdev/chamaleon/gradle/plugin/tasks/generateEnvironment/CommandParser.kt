package io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment

import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Platform
import io.github.gerardorodriguezdev.chamaleon.core.entities.Platform.Property
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyValue
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.CommandParser.CommandParserResult
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.CommandParser.CommandParserResult.Failure
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.CommandParser.CommandParserResult.Success

internal interface CommandParser {
    fun parse(command: String): CommandParserResult

    sealed interface CommandParserResult {
        data class Success(val environment: Environment) : CommandParserResult

        sealed interface Failure : CommandParserResult {
            data class InvalidCommand(val command: String) : Failure
            data class InvalidPlatformType(val command: String, val platformTypeString: String) : Failure
        }
    }

    companion object {
        fun generateCommand(
            environmentName: String,
            platformType: PlatformType,
            properties: List<Property>,
        ): String =
            "$environmentName.${platformType.serialName}.properties[${properties.toStringPairs()}]"

        private fun List<Property>.toStringPairs(): String =
            joinToString(separator = "=") { property -> property.toStringPair() }

        private fun Property.toStringPair(): String = "$name=$value"
    }
}

internal class DefaultCommandParser : CommandParser {

    @Suppress("ReturnCount")
    override fun parse(command: String): CommandParserResult {
        val regexResult = regex.find(command) ?: return Failure.InvalidCommand(command)

        val (environmentNameString, platformTypeString, propertiesString) = regexResult.destructured

        if (environmentNameString.isEmpty()) return Failure.InvalidCommand(command)

        val platformType = platformTypeString.toPlatformType() ?: return Failure.InvalidPlatformType(
            command = command,
            platformTypeString = platformTypeString,
        )
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
            .propertyStrings()
            .mapNotNull { propertyString -> propertyString.toProperty() }
            .toSet()

    private fun String.propertyStrings(): List<String> = split(",")

    @Suppress("ReturnCount")
    private fun String.toProperty(): Property? {
        val propertyStringPair = split("=")

        val propertyNameString = propertyStringPair.firstOrNull()
        if (propertyNameString.isNullOrEmpty()) return null

        val propertyValueString = propertyStringPair.secondOrNull()
        if (propertyValueString.isNullOrEmpty()) return null

        return Property(
            name = propertyNameString,
            value = propertyValueString.toPropertyValue(),
        )
    }

    private fun String.toPropertyValue(): PropertyValue {
        val booleanValue = toBooleanStrictOrNull()
        return if (booleanValue != null) {
            PropertyValue.BooleanProperty(booleanValue)
        } else {
            PropertyValue.StringProperty(this)
        }
    }

    private fun List<String>.secondOrNull(): String? = getOrNull(1)

    companion object {
        val regex = "(.*?)\\.(.*?)\\.properties\\[(.*?)]".toRegex()
    }
}