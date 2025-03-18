package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.theme

import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsProvider
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.BaseTheme
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings.BundleStringsProvider

internal object PluginTheme : BaseTheme {
    override val stringsProvider: StringsProvider = BundleStringsProvider
}