package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.fileTypes

import com.intellij.json.JsonLanguage
import com.intellij.openapi.fileTypes.LanguageFileType
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.icons.Icons
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings.BundleStrings
import javax.swing.Icon

internal class ChamaleonFileType private constructor() : LanguageFileType(JsonLanguage.INSTANCE, true) {
    override fun getName(): String = "Chamaleon"
    override fun getDescription(): String = BundleStrings.fileTypeForChamaleonConfigFiles
    override fun getDefaultExtension(): String = "json"
    override fun getIcon(): Icon = Icons.pluginIcon
}