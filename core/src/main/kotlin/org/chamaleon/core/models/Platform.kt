package org.chamaleon.core.models

data class Platform(
    val platformType: PlatformType,
    val properties: Set<Property>,
) {
    data class Property(
        val name: String,
        val value: PropertyValue?,
    )
}
