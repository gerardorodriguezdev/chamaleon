package io.github.gerardorodriguezdev.chamaleon.core.testing

import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Platform
import io.github.gerardorodriguezdev.chamaleon.core.entities.Platform.Property
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType.*
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyValue.BooleanProperty
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyValue.StringProperty
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema.PropertyDefinition

object TestData {
    const val ENVIRONMENT_NAME = "local"

    val validCompleteSchema = Schema(
        supportedPlatforms = setOf(
            WASM,
            ANDROID,
            JVM,
            IOS,
        ),
        propertyDefinitions = setOf(
            PropertyDefinition(
                name = "HOST",
                propertyType = PropertyType.STRING,
                nullable = true,
            ),
            PropertyDefinition(
                name = "DOMAIN",
                propertyType = PropertyType.STRING,
                nullable = false,
            ),
            PropertyDefinition(
                name = "IS_DEBUG",
                propertyType = PropertyType.BOOLEAN,
                nullable = true,
            ),
            PropertyDefinition(
                name = "IS_PRODUCTION",
                propertyType = PropertyType.BOOLEAN,
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
        name = ENVIRONMENT_NAME,
        platforms = setOf(
            Platform(
                platformType = WASM,
                properties = validCompleteProperties
            ),
            Platform(
                platformType = ANDROID,
                properties = validCompleteProperties
            ),
            Platform(
                platformType = JVM,
                properties = validCompleteProperties
            ),
            Platform(
                platformType = IOS,
                properties = validCompleteProperties
            ),
        )
    )
}