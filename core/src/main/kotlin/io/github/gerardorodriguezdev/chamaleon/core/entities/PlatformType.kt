package io.github.gerardorodriguezdev.chamaleon.core.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
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