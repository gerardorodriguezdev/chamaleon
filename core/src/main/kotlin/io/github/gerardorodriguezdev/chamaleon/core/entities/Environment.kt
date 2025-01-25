package io.github.gerardorodriguezdev.chamaleon.core.entities

public data class Environment(
    val name: String,
    val platforms: Set<Platform>,
) {
    public fun wasmPlatform(): Platform = platform(PlatformType.WASM)
    public fun wasmPlatformOrNull(): Platform? = platformOrNull(PlatformType.WASM)

    public fun androidPlatform(): Platform = platform(PlatformType.ANDROID)
    public fun androidPlatformOrNull(): Platform? = platformOrNull(PlatformType.ANDROID)

    public fun iosPlatform(): Platform = platform(PlatformType.IOS)
    public fun iosPlatformOrNull(): Platform? = platformOrNull(PlatformType.IOS)

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