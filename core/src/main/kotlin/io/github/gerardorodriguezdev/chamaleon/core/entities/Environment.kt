package io.github.gerardorodriguezdev.chamaleon.core.entities

@Suppress("TooManyFunctions")
public data class Environment(
    val name: String,
    val platforms: Set<Platform>,
) {
    public fun androidPlatform(): Platform = platform(PlatformType.ANDROID)
    public fun androidPlatformOrNull(): Platform? = platformOrNull(PlatformType.ANDROID)

    public fun wasmPlatform(): Platform = platform(PlatformType.WASM)
    public fun wasmPlatformOrNull(): Platform? = platformOrNull(PlatformType.WASM)

    public fun jsPlatform(): Platform = platform(PlatformType.JS)
    public fun jsPlatformOrNull(): Platform? = platformOrNull(PlatformType.JS)

    public fun nativePlatform(): Platform = platform(PlatformType.NATIVE)
    public fun nativePlatformOrNull(): Platform? = platformOrNull(PlatformType.NATIVE)

    public fun jvmPlatform(): Platform = platform(PlatformType.JVM)
    public fun jvmPlatformOrNull(): Platform? = platformOrNull(PlatformType.JVM)

    private fun Environment.platform(platformType: PlatformType): Platform =
        platforms.first { platform ->
            platform.platformType == platformType
        }

    private fun Environment.platformOrNull(platformType: PlatformType): Platform? =
        platforms.firstOrNull { platform ->
            platform.platformType == platformType
        }
}