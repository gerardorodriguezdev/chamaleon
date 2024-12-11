package io.github.gerardorodriguezdev.chamaleon.core.models

data class Environment(
    val name: String,
    val platforms: Set<Platform>,
) {
    val wasmPlatform: Platform get() = platform(PlatformType.wasm)
    val androidPlatform: Platform get() = platform(PlatformType.android)
    val iosPlatform: Platform get() = platform(PlatformType.ios)
    val jvmPlatform: Platform get() = platform(PlatformType.jvm)

    private fun Environment.platform(platformType: PlatformType): Platform =
        platforms.first { platform ->
            platform.platformType == platformType
        }
}