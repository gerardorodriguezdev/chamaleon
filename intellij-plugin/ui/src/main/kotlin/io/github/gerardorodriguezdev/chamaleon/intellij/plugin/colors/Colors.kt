package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.colors

import androidx.compose.ui.graphics.Color

interface Colors {
    val infoBorderColor: Color
}

object DefaultColors : Colors {
    override val infoBorderColor: Color = Color.Black
}