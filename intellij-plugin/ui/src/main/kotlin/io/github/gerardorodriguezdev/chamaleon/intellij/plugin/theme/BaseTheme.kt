package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.DefaultStringsProvider
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsKeys.StringKey
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsProvider

interface BaseTheme {
    val stringsProvider: StringsProvider

    @Composable
    fun Theme(content: @Composable () -> Unit) {
        val stringsProvider = remember { stringsProvider }
        CompositionLocalProvider(
            LocalStringsProvider provides stringsProvider,
        ) {
            content()
        }
    }
}

@Suppress("CompositionLocalAllowlist")
private val LocalStringsProvider = staticCompositionLocalOf<StringsProvider> { DefaultStringsProvider }

@Composable
fun string(stringKey: StringKey): String {
    val string = LocalStringsProvider.current.string(stringKey)
    return remember { string }
}