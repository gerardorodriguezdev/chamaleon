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
    const val LOCAL_ENVIRONMENT_NAME = "local"
    const val PRODUCTION_ENVIRONMENT_NAME = "production"
    const val HOST_PROPERTY_NAME = "HOST"
    const val DOMAIN_PROPERTY_NAME = "DOMAIN"
    const val IS_DEBUG_PROPERTY_NAME = "IS_DEBUG"
    const val IS_PRODUCTION_PROPERTY_NAME = "IS_PRODUCTION"
    const val DOMAIN = "www.domain.com"

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
        value = StringProperty(DOMAIN),
    )

    val debugProperty = Property(
        name = IS_DEBUG_PROPERTY_NAME,
        value = null,
    )

    val productionProperty = Property(
        name = IS_PRODUCTION_PROPERTY_NAME,
        value = BooleanProperty(true),
    )

    val schema = Schema(
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

    val schemaWithRestrictedPlatform = Schema(
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

    val properties = setOf(
        hostProperty,
        domainProperty,
        debugProperty,
        productionProperty,
    )

    val wasmPlatform = Platform(
        platformType = WASM,
        properties = properties
    )

    val androidPlatform = Platform(
        platformType = ANDROID,
        properties = properties
    )

    val jvmPlatform = Platform(
        platformType = JVM,
        properties = properties
    )

    val iosPlatform = Platform(
        platformType = IOS,
        properties = properties
    )

    val environment = Environment(
        name = LOCAL_ENVIRONMENT_NAME,
        platforms = setOf(
            wasmPlatform,
            androidPlatform,
            jvmPlatform,
            iosPlatform,
        )
    )

    val environmentWithRestrictedPlatform = Environment(
        name = LOCAL_ENVIRONMENT_NAME,
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