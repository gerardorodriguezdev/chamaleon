package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.extensions

import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.Verification

fun Verification?.isValid(): Boolean = this is Verification.Valid