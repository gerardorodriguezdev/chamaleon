package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.plugin.fileTypes

import com.intellij.json.JsonLanguage
import com.intellij.openapi.fileTypes.LanguageFileType
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.plugin.icons.Icons
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.plugin.messages.Bundle
import javax.swing.Icon

internal class ChamaleonFileType private constructor() : LanguageFileType(JsonLanguage.INSTANCE, true) {
    override fun getName(): String = "Chamaleon"
    override fun getDescription(): String = Bundle.fileTypeForChamaleonConfigFiles
    override fun getDefaultExtension(): String = "json"
    override fun getIcon(): Icon = Icons.pluginIcon
}