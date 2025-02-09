package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings

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
    val validEnvironments = StringKey("valid.environments")
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

    @JvmInline
    value class StringKey internal constructor(@PropertyKey(resourceBundle = "messages.Bundle") val value: String)
}