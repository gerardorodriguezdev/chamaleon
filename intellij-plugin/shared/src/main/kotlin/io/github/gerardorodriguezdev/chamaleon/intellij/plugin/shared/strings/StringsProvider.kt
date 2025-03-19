package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings

import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsKeys.StringKey

interface StringsProvider {
    fun string(key: StringKey): String
}

object DefaultStringsProvider : StringsProvider {
    override fun string(key: StringKey): String = key.value
}