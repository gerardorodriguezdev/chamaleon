package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.theme

import androidx.compose.runtime.Composable
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.BaseTheme
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.colors.PluginColors
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings.BundleStrings

@Composable
internal fun PluginTheme(content: @Composable () -> Unit) {
    BaseTheme(strings = BundleStrings, colors = PluginColors, content = content)
}