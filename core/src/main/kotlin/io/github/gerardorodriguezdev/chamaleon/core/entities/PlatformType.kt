package io.github.gerardorodriguezdev.chamaleon.core.entities

import kotlinx.serialization.SerialName

public enum class PlatformType {
    @SerialName("android")
    ANDROID,

    @SerialName("wasm")
    WASM,

    @SerialName("ios")
    IOS,

    @SerialName("jvm")
    JVM
}