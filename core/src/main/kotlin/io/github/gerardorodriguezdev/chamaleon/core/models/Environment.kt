package io.github.gerardorodriguezdev.chamaleon.core.models

public data class Environment(
    val name: String,
    val platforms: Set<Platform>,
) {
    public val androidPlatform: Platform get() = platform(PlatformType.ANDROID)
    public val androidPlatformOrNull: Platform? = platformOrNull(PlatformType.ANDROID)

    public val wasmPlatform: Platform get() = platform(PlatformType.WASM)
    public val wasmPlatformOrNull: Platform? = platformOrNull(PlatformType.WASM)

    public val jsPlatform: Platform get() = platform(PlatformType.JS)
    public val jsPlatformOrNull: Platform? = platformOrNull(PlatformType.JS)

    public val nativePlatform: Platform get() = platform(PlatformType.NATIVE)
    public val nativePlatformOrNull: Platform? = platformOrNull(PlatformType.NATIVE)

    public val jvmPlatform: Platform get() = platform(PlatformType.JVM)
    public val jvmPlatformOrNull: Platform? = platformOrNull(PlatformType.JVM)

    private fun Environment.platform(platformType: PlatformType): Platform =
        platforms.first { platform ->
            platform.platformType == platformType
        }

    private fun Environment.platformOrNull(platformType: PlatformType): Platform? =
        platforms.firstOrNull { platform ->
            platform.platformType == platformType
        }
}