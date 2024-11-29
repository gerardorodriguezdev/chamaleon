package org.chamaleon.core.testing

import org.chamaleon.core.models.Environment
import org.chamaleon.core.models.Platform
import org.chamaleon.core.models.Platform.Property
import org.chamaleon.core.models.PlatformType.*
import org.chamaleon.core.models.PropertyType
import org.chamaleon.core.models.PropertyValue.BooleanProperty
import org.chamaleon.core.models.PropertyValue.StringProperty
import org.chamaleon.core.models.Schema
import org.chamaleon.core.models.Schema.PropertyDefinition

object TestData {
    const val ENVIRONMENTS_FILE_NAME = "local-environment.json"

    val validCompleteSchema = Schema(
        supportedPlatforms = setOf(
            wasm,
            android,
            jvm,
            ios,
        ),
        propertyDefinitions = setOf(
            PropertyDefinition(
                name = "HOST",
                propertyType = PropertyType.String,
                nullable = true,
            ),
            PropertyDefinition(
                name = "DOMAIN",
                propertyType = PropertyType.String,
                nullable = false,
            ),
            PropertyDefinition(
                name = "IS_DEBUG",
                propertyType = PropertyType.Boolean,
                nullable = true,
            ),
            PropertyDefinition(
                name = "IS_PRODUCTION",
                propertyType = PropertyType.Boolean,
                nullable = false,
            )
        )
    )

    private val validCompleteProperties = setOf(
        Property(
            name = "HOST",
            value = null,
        ),
        Property(
            name = "DOMAIN",
            value = StringProperty("www.domain.com"),
        ),
        Property(
            name = "IS_DEBUG",
            value = null,
        ),
        Property(
            name = "IS_PRODUCTION",
            value = BooleanProperty(true),
        ),
    )

    val validCompleteEnvironment = Environment(
        name = ENVIRONMENTS_FILE_NAME,
        platforms = setOf(
            Platform(
                platformType = wasm,
                properties = validCompleteProperties
            ),
            Platform(
                platformType = android,
                properties = validCompleteProperties
            ),
            Platform(
                platformType = jvm,
                properties = validCompleteProperties
            ),
            Platform(
                platformType = ios,
                properties = validCompleteProperties
            ),
        )
    )
}