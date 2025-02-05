package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings

interface Strings {
    val environmentSelectionWindowName: String
    val environmentsDirectoryPath: String
    val selectedEnvironment: String
    val removeSelectedEnvironment: String
    val refreshEnvironments: String
    val fileTypeForChamaleonConfigFiles: String
    val createEnvironment: String
    val cancel: String
    val previous: String
    val next: String
    val finish: String
    val selectEnvironmentsDirectoryLocation: String
    val environmentsDirectoryLocation: String
}

object DefaultStrings : Strings {
    override val environmentSelectionWindowName: String = "Environment selection"
    override val environmentsDirectoryPath: String = "Environments directory path:"
    override val selectedEnvironment: String = "Selected environment:"
    override val removeSelectedEnvironment: String = "Remove selected environment"
    override val refreshEnvironments: String = "Refresh environments"
    override val fileTypeForChamaleonConfigFiles: String = "File type for Chamaleon config files"
    override val createEnvironment: String = "Create environment"
    override val cancel: String = "Cancel"
    override val previous: String = "Previous"
    override val next: String = "Next"
    override val finish: String = "Finish"
    override val selectEnvironmentsDirectoryLocation: String = "Select the environments directory location"
    override val environmentsDirectoryLocation: String = "Environments directory location:"
}