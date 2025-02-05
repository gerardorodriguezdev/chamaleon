package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.theme

import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.colors.Colors
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.Strings
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.BaseTheme
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.colors.PluginColors
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings.BundleStrings

internal object PluginTheme : BaseTheme {
    override val strings: Strings = BundleStrings
    override val colors: Colors = PluginColors
}