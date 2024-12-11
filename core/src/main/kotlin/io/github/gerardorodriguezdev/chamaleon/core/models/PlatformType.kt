package io.github.gerardorodriguezdev.chamaleon.core.models

import kotlinx.serialization.SerialName

enum class PlatformType {
    @SerialName("android")
    ANDROID,

    @SerialName("wasm")
    WASM,

    @SerialName("ios")
    IOS,

    @SerialName("jvm")
    JVM
}