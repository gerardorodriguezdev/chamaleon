package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.annotations.PropertyKey

object StringsKeys {
    val environmentSelectionWindowName = StringKey("environment.selection.window.name")
    val environmentsDirectoryPath = StringKey("environments.directory.path")
    val selectedEnvironment = StringKey("selected.environment")
    val removeSelectedEnvironment = StringKey("remove.selected.environment")
    val refreshEnvironments = StringKey("refresh.environments")
    val fileTypeForChamaleonConfigFiles = StringKey("file.type.for.chamaleon.config.files")
    val createEnvironment = StringKey("create.environment")
    val cancel = StringKey("cancel")
    val previous = StringKey("previous")
    val next = StringKey("next")
    val finish = StringKey("finish")
    val selectEnvironmentsDirectoryLocation = StringKey("select.environments.directory.location")
    val environmentsDirectory = StringKey("environments.directory")
    val createTemplate = StringKey("create.template")
    val updateTemplate = StringKey("update.template")
    val selectedTemplate = StringKey("selected.template")
    val validField = StringKey("valid.field")
    val supportedPlatforms = StringKey("supported.platforms")
    val propertyDefinitions = StringKey("property.definitions")
    val addPropertyDefinitions = StringKey("add.property.definitions")
    val propertyName = StringKey("property.name")
    val propertyType = StringKey("property.type")
    val nullable = StringKey("nullable")
    val supportedPlatformsForPropertyDefinitions = StringKey("supported.platforms.for.property.definitions")
    val environmentName = StringKey("environment.name")
    val properties = StringKey("properties")
    val name = StringKey("name")
    val value = StringKey("value")
    val addProperty = StringKey("add.property")
    val setupEnvironment = StringKey("setup.environment")
    val addProperties = StringKey("add.properties")
    val clearText = StringKey("clear.text")
    val invalidEnvironmentsFound = StringKey("invalid.environments.found")
    val selectedFileNotDirectory = StringKey("selected.file.not.directory")
    val environmentNameEmpty = StringKey("environment.name.empty")
    val environmentNameIsDuplicated = StringKey("environment.name.is.duplicated")
    val propertyNameIsEmpty = StringKey("property.name.is.empty")
    val deletePropertyDefinition = StringKey("delete.property.definition")
    val noEnvironmentsFound = StringKey("no.environments.found")
    val errorAtEnvironmentsDirectories = StringKey("error.at.environments.directories")
    val gradlePluginVersionUsed = StringKey("gradle.plugin.version.used")
    val generateEnvironment = StringKey("generate.environment")

    fun something(value: String, other: String) = StringKey(value, persistentListOf(other))

    class StringKey internal constructor(
        @PropertyKey(resourceBundle = "messages.Bundle") val value: String,
        val params: ImmutableList<Any> = persistentListOf(),
    ) {
        override fun equals(other: Any?): Boolean =
            when {
                this === other -> true
                other !is StringKey -> false
                value != other.value -> false
                params != other.params -> false
                else -> true
            }

        override fun hashCode(): Int {
            var result = value.hashCode()
            result = 31 * result + params.hashCode()
            return result
        }
    }
}