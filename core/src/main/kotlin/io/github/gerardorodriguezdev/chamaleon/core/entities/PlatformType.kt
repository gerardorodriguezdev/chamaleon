package io.github.gerardorodriguezdev.chamaleon.core.entities

import io.github.gerardorodriguezdev.chamaleon.core.serializers.PlatformTypeSerializer
import kotlinx.serialization.Serializable

@Serializable(with = PlatformTypeSerializer::class)
public enum class PlatformType(public val serialName: String) {
    ANDROID("android"),
    WASM("wasm"),
    JS("js"),
    NATIVE("native"),
    JVM("jvm")
}