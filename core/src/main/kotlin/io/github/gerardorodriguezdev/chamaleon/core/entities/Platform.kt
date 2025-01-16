package io.github.gerardorodriguezdev.chamaleon.core.entities

public data class Platform(
    val platformType: PlatformType,
    val properties: Set<Property>,
) {
    public data class Property(
        val name: String,
        val value: PropertyValue?,
    )
}