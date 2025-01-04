package io.github.gerardorodriguezdev.chamaleon.core.models

public data class Platform(
    val platformType: PlatformType,
    val properties: Set<Property>,
) {
    public data class Property(
        val name: String,
        val value: PropertyValue?,
    )
}