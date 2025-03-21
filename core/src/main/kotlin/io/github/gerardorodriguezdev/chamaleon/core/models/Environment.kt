package io.github.gerardorodriguezdev.chamaleon.core.models

import io.github.gerardorodriguezdev.chamaleon.core.safeModels.KeyProvider
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString

public data class Environment(
    val name: NonEmptyString,
    val platforms: NonEmptyKeySetStore<PlatformType, Platform>,
) : KeyProvider<String> {
    override val key: String = name.value

    val androidPlatform: Platform get() = platform(PlatformType.ANDROID)
    val androidPlatformOrNull: Platform? = platformOrNull(PlatformType.ANDROID)

    val wasmPlatform: Platform get() = platform(PlatformType.WASM)
    val wasmPlatformOrNull: Platform? = platformOrNull(PlatformType.WASM)

    val jsPlatform: Platform get() = platform(PlatformType.JS)
    val jsPlatformOrNull: Platform? = platformOrNull(PlatformType.JS)

    val nativePlatform: Platform get() = platform(PlatformType.NATIVE)
    val nativePlatformOrNull: Platform? = platformOrNull(PlatformType.NATIVE)

    val jvmPlatform: Platform get() = platform(PlatformType.JVM)
    val jvmPlatformOrNull: Platform? = platformOrNull(PlatformType.JVM)

    private fun platform(platformType: PlatformType): Platform = platforms.getValue(platformType)

    private fun platformOrNull(platformType: PlatformType): Platform? = platforms[platformType]
}