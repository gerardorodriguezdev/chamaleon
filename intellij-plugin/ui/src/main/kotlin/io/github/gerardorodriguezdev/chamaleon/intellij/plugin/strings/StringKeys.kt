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
    val environmentsDirectoryLocation = StringKey("environments.directory.location")
    val createSchema = StringKey("create.schema")
    val updateSchema = StringKey("update.schema")
    val validEnvironments = StringKey("valid.environments")
    val supportedPlatforms = StringKey("supported.platforms")
    val propertyDefinitions = StringKey("property.definitions")
    val addPropertyDefinitions = StringKey("add.property.definitions")
    val propertyName = StringKey("property.name")
    val propertyType = StringKey("property.type")
    val nullable = StringKey("nullable")
    val supportedPlatformsForPropertyDefinitions = StringKey("supported.platforms.for.property.definitions")

    @JvmInline
    value class StringKey internal constructor(@PropertyKey(resourceBundle = "messages.Bundle") val value: String)
}