package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.messages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey
import kotlin.reflect.KProperty

private const val BUNDLE = "messages.Bundle"

object Bundle {
    private val INSTANCE = DynamicBundle(Bundle::class.java, BUNDLE)

    val environmentSelectionWindowName by messageDelegate("environment.selection.window.name")

    val fileTypeForChamaleonConfigFiles by messageDelegate("fileType.for.chamaleon.config.files")

    @get:Composable
    val environmentsDirectoryPath by rememberedMessageDelegate("environments.directory.path")

    @get:Composable
    val selectedEnvironment by rememberedMessageDelegate("selected.environment")

    @get:Composable
    val removeSelectedEnvironment by rememberedMessageDelegate("remove.selected.environment")

    @get:Composable
    val refreshEnvironments by rememberedMessageDelegate("refresh.environments")

    private fun message(
        key:
        @PropertyKey(resourceBundle = BUNDLE)
        String,
        vararg params: Any
    ): @Nls String = INSTANCE.getMessage(key, *params)

    @Composable
    private fun rememberMessage(
        @PropertyKey(resourceBundle = BUNDLE) key: String,
        vararg params: Any,
    ): String = remember { message(key, params) }

    private fun messageDelegate(
        key:
        @PropertyKey(resourceBundle = BUNDLE)
        String
    ): MessageDelegate =
        MessageDelegate(key)

    private fun rememberedMessageDelegate(
        key:
        @PropertyKey(resourceBundle = BUNDLE)
        String
    ): RememberedMessageDelegate =
        RememberedMessageDelegate(key)

    private class MessageDelegate(
        private val key:
        @PropertyKey(resourceBundle = BUNDLE)
        String
    ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): String = message(key)
    }

    private class RememberedMessageDelegate(
        private val key:
        @PropertyKey(resourceBundle = BUNDLE)
        String
    ) {
        @Composable
        operator fun getValue(thisRef: Any?, property: KProperty<*>): String = rememberMessage(key)
    }
}