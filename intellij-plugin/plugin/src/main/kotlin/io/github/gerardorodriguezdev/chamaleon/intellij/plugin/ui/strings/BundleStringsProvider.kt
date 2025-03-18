package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings

import com.intellij.DynamicBundle
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsKeys.StringKey
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsProvider
import org.jetbrains.annotations.PropertyKey

internal object BundleStringsProvider : StringsProvider {
    private const val BUNDLE = "messages.Bundle"

    private val INSTANCE = DynamicBundle(BundleStringsProvider::class.java, BUNDLE)

    override fun string(key: StringKey, params: Array<Any>): String = message(key.value, params)

    @Suppress("SpreadOperator")
    private fun message(
        key:
        @PropertyKey(resourceBundle = BUNDLE)
        String,
        params: Array<Any>
    ): String = INSTANCE.getMessage(key, *params)
}