package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.plugin.previews.utils

import org.jetbrains.jewel.ui.icon.IconKey

internal object FakeIconKey : IconKey {
    override val iconClass: Class<*> = this@FakeIconKey::class.java
    override fun path(isNewUi: Boolean): String = ""
}