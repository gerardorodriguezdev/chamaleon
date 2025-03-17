package io.github.gerardorodriguezdev.chamaleon.gradle.plugin.extensions

import org.gradle.api.logging.Logger

internal fun Logger.chamaleonLog(message: String) {
    lifecycle("[Chamaleon] $message")
}