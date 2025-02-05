package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.colors

import androidx.compose.ui.graphics.Color
import com.intellij.util.ui.JBUI
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.colors.Colors
import org.jetbrains.jewel.bridge.toComposeColor

internal object PluginColors : Colors {
    override val infoBorderColor: Color = JBUI.CurrentTheme.Banner.INFO_BORDER_COLOR.toComposeColor()
}