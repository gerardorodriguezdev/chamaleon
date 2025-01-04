package io.github.gerardorodriguezdev.chamaleon.core.entities

public data class Environment(
    val name: String,
    val platforms: Set<Platform>,
) {
    val wasmPlatform: Platform get() = platform(PlatformType.WASM)
    val androidPlatform: Platform get() = platform(PlatformType.ANDROID)
    val iosPlatform: Platform get() = platform(PlatformType.IOS)
    val jvmPlatform: Platform get() = platform(PlatformType.JVM)

    private fun Environment.platform(platformType: PlatformType): Platform =
        platforms.first { platform ->
            platform.platformType == platformType
        }
}