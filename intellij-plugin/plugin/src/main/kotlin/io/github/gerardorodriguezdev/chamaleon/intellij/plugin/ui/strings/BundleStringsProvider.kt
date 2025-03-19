package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings

import com.intellij.DynamicBundle
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsKeys.StringKey
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsProvider
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.annotations.PropertyKey

internal object BundleStringsProvider : StringsProvider {
    private const val BUNDLE = "messages.Bundle"

    private val INSTANCE = DynamicBundle(BundleStringsProvider::class.java, BUNDLE)

    override fun string(key: StringKey): String = message(key.value, key.params)

    @Suppress("SpreadOperator")
    private fun message(
        key:
        @PropertyKey(resourceBundle = BUNDLE)
        String,
        params: ImmutableList<Any>,
    ): String = INSTANCE.getMessage(key, *params.toTypedArray())
}