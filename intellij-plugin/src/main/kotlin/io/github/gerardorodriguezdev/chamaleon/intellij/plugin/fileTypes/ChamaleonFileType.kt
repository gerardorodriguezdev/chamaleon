package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.fileTypes

import com.intellij.json.JsonLanguage
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsSafe
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.icons.Icons
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.messages.Bundle
import org.jetbrains.annotations.NonNls
import javax.swing.Icon

class ChamaleonFileType private constructor() : LanguageFileType(JsonLanguage.INSTANCE, true) {
    override fun getName(): @NonNls String = "Chamaleon"

    override fun getDescription(): @NlsContexts.Label String = Bundle.fileTypeForChamaleonConfigFiles

    override fun getDefaultExtension(): @NlsSafe String = "json"

    override fun getIcon(): Icon? = Icons.pluginIcon
}