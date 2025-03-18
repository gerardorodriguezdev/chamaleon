package io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform.Property
import io.github.gerardorodriguezdev.chamaleon.core.models.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore.Companion.toNonEmptyKeySetStore
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString.Companion.toNonEmptyString
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.CommandParser.CommandParserResult
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.CommandParser.CommandParserResult.Failure
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.tasks.generateEnvironment.CommandParser.CommandParserResult.Success

internal interface CommandParser {
    fun parse(commands: List<String>): CommandParserResult

    sealed interface CommandParserResult {
        data class Success(val environments: NonEmptyKeySetStore<String, Environment>) : CommandParserResult
        data class Failure(val errorMessage: String) : CommandParserResult
    }

    companion object {
        fun create(): CommandParser = DefaultCommandParser()

        fun generateCommand(
            environmentName: String,
            platformType: PlatformType,
            properties: List<Property>,
        ): String =
            "$environmentName.${platformType.serialName}.properties[${properties.toStringPairs()}]"

        private fun List<Property>.toStringPairs(): String =
            joinToString(separator = "=") { property -> property.toStringPair() }

        private fun Property.toStringPair(): String = "${name.value}=$value"
    }
}

@Suppress("TooManyFunctions")
internal class DefaultCommandParser : CommandParser {

    override fun parse(commands: List<String>): CommandParserResult =
        either {
            val environments = commands
                .map { command -> parseCommand(command).bind() }
                .mergeDuplicatedEnvironments(commands)
                .bind()

            Success(environments = environments)
        }.fold(
            ifLeft = { it },
            ifRight = { it },
        )

    private fun parseCommand(command: String): Either<Failure, Environment> =
        either {
            val context = context(command).bind()

            Environment(
                name = context.toEnvironmentName().bind(),
                platforms = context.platforms().bind(),
            )
        }

    private fun context(command: String): Either<Failure, Context> =
        either {
            val regexResult = regex.find(command)
            ensureNotNull(regexResult) { Failure("Command '$command' didn't match pattern '${regex.pattern}") }

            val (environmentNameString, platformTypeString, propertiesString) = regexResult.destructured
            Context(
                command = command,
                environmentNameString = environmentNameString,
                platformTypeString = platformTypeString,
                propertiesString = propertiesString,
            )
        }

    private fun Context.toEnvironmentName(): Either<Failure, NonEmptyString> =
        either {
            val environmentName = environmentNameString.toNonEmptyString()
            ensureNotNull(environmentName) { Failure("Empty environment name on command '$command'") }
        }

    private fun Context.platforms(): Either<Failure, NonEmptyKeySetStore<PlatformType, Platform>> =
        either {
            val platformType = toPlatformType()
            val properties = toProperties()

            val platforms = setOf(
                Platform(
                    platformType = platformType.bind(),
                    properties = properties.bind(),
                )
            ).toNonEmptyKeySetStore()
            ensureNotNull(platforms) { Failure("Platforms not found on command '$command'") }
        }

    private fun Context.toPlatformType(): Either<Failure, PlatformType> =
        either {
            val platformType = PlatformType.values().firstOrNull { platformType ->
                platformType.serialName == platformTypeString
            }

            ensureNotNull(platformType) {
                Failure("Invalid platform type '$platformTypeString' on command '$command'")
            }
        }

    private fun Context.toProperties(): Either<Failure, NonEmptyKeySetStore<String, Property>> =
        either {
            val properties = propertiesString
                .propertyStrings()
                .map { propertyString ->
                    val property = toProperty(propertyString)
                    property.bind()
                }
                .toNonEmptyKeySetStore()

            ensureNotNull(properties) { Failure("No properties found on command '$command'") }
        }

    private fun String.propertyStrings(): List<String> = split(",")

    private fun Context.toProperty(propertyString: String): Either<Failure, Property> =
        either {
            val propertyStringPair = propertyString.split("=")

            val propertyNameString = propertyStringPair.firstOrNull()?.toNonEmptyString()
            ensureNotNull(propertyNameString) {
                Failure("No property name found on property '$propertyStringPair' on command '$command'")
            }

            val propertyValueString = propertyStringPair.secondOrNull()?.toNonEmptyString()
            ensureNotNull(propertyValueString) {
                Failure("No property value found on property '$propertyStringPair' on command '$command'")
            }

            Property(
                name = propertyNameString,
                value = propertyValueString.toPropertyValue()
            )
        }

    private fun NonEmptyString.toPropertyValue(): PropertyValue {
        val booleanValue = value.toBooleanStrictOrNull()

        return if (booleanValue != null) {
            PropertyValue.BooleanProperty(booleanValue)
        } else {
            PropertyValue.StringProperty(this)
        }
    }

    private fun List<String>.secondOrNull(): String? = getOrNull(1)

    private fun List<Environment>.mergeDuplicatedEnvironments(
        commands: List<String>,
    ): Either<Failure, NonEmptyKeySetStore<String, Environment>> =
        either {
            val environments =
                this@mergeDuplicatedEnvironments
                    .groupBy { environment -> environment.name }
                    .map { (environmentName, environments) ->
                        val allPlatforms = environments.map { environment -> environment.platforms }
                        Environment(
                            name = environmentName,
                            platforms = allPlatforms.mergePlatforms()
                        )
                    }
                    .toNonEmptyKeySetStore()

            ensureNotNull(environments) {
                Failure("No environments found on commands '$commands'")
            }
        }

    @Suppress("MaxLineLength")
    private fun List<NonEmptyKeySetStore<PlatformType, Platform>>.mergePlatforms(): NonEmptyKeySetStore<PlatformType, Platform> =
        reduce { accumulation, platforms ->
            accumulation.addAll(platforms)
        }

    private data class Context(
        val command: String,
        val environmentNameString: String,
        val platformTypeString: String,
        val propertiesString: String,
    )

    private companion object {
        val regex = "(.*?)\\.(.*?)\\.properties\\[(.*?)]".toRegex()
    }
}