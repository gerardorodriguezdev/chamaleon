package io.github.gerardorodriguezdev.chamaleon.core.entities

public data class Environment(
    val name: String,
    val platforms: Set<Platform>,
) {
    public fun wasmPlatform(): Platform = platform(PlatformType.WASM)
    public fun androidPlatform(): Platform = platform(PlatformType.ANDROID)
    public fun iosPlatform(): Platform = platform(PlatformType.IOS)
    public fun jvmPlatform(): Platform = platform(PlatformType.JVM)

    private fun Environment.platform(platformType: PlatformType): Platform =
        platforms.first { platform ->
            platform.platformType == platformType
        }
}