package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.colors.Colors
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.colors.DefaultColors
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.DefaultStrings
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.Strings

val LocalStrings = staticCompositionLocalOf<Strings> { DefaultStrings }

val LocalColors = staticCompositionLocalOf<Colors> { DefaultColors }

@Composable
fun BaseTheme(
    strings: Strings = DefaultStrings,
    colors: Colors = DefaultColors,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalColors provides colors,
        LocalStrings provides strings,
    ) {
        content()
    }
}