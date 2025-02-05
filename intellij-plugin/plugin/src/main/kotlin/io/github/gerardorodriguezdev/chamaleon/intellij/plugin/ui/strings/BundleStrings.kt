package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings

import com.intellij.DynamicBundle
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.Strings
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey
import kotlin.reflect.KProperty

internal object BundleStrings : Strings {
    private const val BUNDLE = "messages.Bundle"
    private val INSTANCE = DynamicBundle(BundleStrings::class.java, BUNDLE)

    override val environmentSelectionWindowName by messageDelegate("environment.selection.window.name")
    override val environmentsDirectoryPath by messageDelegate("environments.directory.path")
    override val selectedEnvironment by messageDelegate("selected.environment")
    override val removeSelectedEnvironment by messageDelegate("remove.selected.environment")
    override val refreshEnvironments by messageDelegate("refresh.environments")
    override val fileTypeForChamaleonConfigFiles by messageDelegate("file.type.for.chamaleon.config.files")
    override val createEnvironment by messageDelegate("create.environment")
    override val cancel by messageDelegate("cancel")
    override val previous by messageDelegate("previous")
    override val next by messageDelegate("next")
    override val finish by messageDelegate("finish")

    private fun message(
        key:
        @PropertyKey(resourceBundle = BUNDLE)
        String,
        vararg params: Any
    ): @Nls String = INSTANCE.getMessage(key, *params)

    private fun messageDelegate(
        key:
        @PropertyKey(resourceBundle = BUNDLE)
        String
    ): MessageDelegate =
        MessageDelegate(key)

    private class MessageDelegate(
        private val key:
        @PropertyKey(resourceBundle = BUNDLE)
        String
    ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): String = message(key)
    }
}