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
    const val HOST_PROPERTY_NAME = "HOST"
    const val DOMAIN_PROPERTY_NAME = "DOMAIN"
    const val IS_DEBUG_PROPERTY_NAME = "IS_DEBUG"
    const val IS_PRODUCTION_PROPERTY_NAME = "IS_PRODUCTION"

    val allPlatforms = setOf(
        WASM,
        ANDROID,
        JVM,
        IOS,
    )

    val hostProperty = Property(
        name = HOST_PROPERTY_NAME,
        value = null,
    )

    val domainProperty = Property(
        name = DOMAIN_PROPERTY_NAME,
        value = StringProperty("www.domain.com"),
    )

    val validCompleteSchema = Schema(
        supportedPlatforms = allPlatforms,
        propertyDefinitions = setOf(
            PropertyDefinition(
                name = HOST_PROPERTY_NAME,
                propertyType = PropertyType.STRING,
                nullable = true,
                supportedPlatforms = emptySet(),
            ),
            PropertyDefinition(
                name = DOMAIN_PROPERTY_NAME,
                propertyType = PropertyType.STRING,
                nullable = false,
                supportedPlatforms = emptySet(),
            ),
            PropertyDefinition(
                name = IS_DEBUG_PROPERTY_NAME,
                propertyType = PropertyType.BOOLEAN,
                nullable = true,
                supportedPlatforms = emptySet(),
            ),
            PropertyDefinition(
                name = IS_PRODUCTION_PROPERTY_NAME,
                propertyType = PropertyType.BOOLEAN,
                nullable = false,
                supportedPlatforms = emptySet(),
            )
        )
    )

    val validSchemaWithRestrictedPlatform = Schema(
        supportedPlatforms = setOf(
            ANDROID,
            JVM,
        ),
        propertyDefinitions = setOf(
            PropertyDefinition(
                name = HOST_PROPERTY_NAME,
                propertyType = PropertyType.STRING,
                nullable = true,
                supportedPlatforms = setOf(ANDROID)
            ),
            PropertyDefinition(
                name = DOMAIN_PROPERTY_NAME,
                propertyType = PropertyType.STRING,
                nullable = false,
                supportedPlatforms = emptySet(),
            ),
        )
    )

    val validCompleteProperties = setOf(
        Property(
            name = HOST_PROPERTY_NAME,
            value = null,
        ),
        Property(
            name = DOMAIN_PROPERTY_NAME,
            value = StringProperty("www.domain.com"),
        ),
        Property(
            name = IS_DEBUG_PROPERTY_NAME,
            value = null,
        ),
        Property(
            name = IS_PRODUCTION_PROPERTY_NAME,
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

    val validEnvironmentWithRestrictedPlatform = Environment(
        name = ENVIRONMENT_NAME,
        platforms = setOf(
            Platform(
                platformType = ANDROID,
                properties = setOf(
                    hostProperty,
                    domainProperty,
                )
            ),
            Platform(
                platformType = JVM,
                properties = setOf(
                    domainProperty
                )
            ),
        )
    )
}