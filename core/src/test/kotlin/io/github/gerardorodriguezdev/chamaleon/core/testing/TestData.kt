package io.github.gerardorodriguezdev.chamaleon.core.testing

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform.Property
import io.github.gerardorodriguezdev.chamaleon.core.models.PlatformType.*
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyType
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.BooleanProperty
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.StringProperty
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema.PropertyDefinition

object TestData {
    const val LOCAL_ENVIRONMENT_NAME = "local"
    const val PRODUCTION_ENVIRONMENT_NAME = "production"
    const val HOST_PROPERTY_NAME = "HOST"
    const val DOMAIN_PROPERTY_NAME = "DOMAIN"
    const val IS_DEBUG_PROPERTY_NAME = "IS_DEBUG"
    const val IS_PRODUCTION_PROPERTY_NAME = "IS_PRODUCTION"
    const val DOMAIN = "www.domain.com"
    val localEnvironmentFileName = EnvironmentsProcessor.environmentFileName(LOCAL_ENVIRONMENT_NAME)
    val productionEnvironmentFileName =
        EnvironmentsProcessor.environmentFileName(PRODUCTION_ENVIRONMENT_NAME)

    val allPlatformsTypes = setOf(
        ANDROID,
        WASM,
        JS,
        JVM,
        NATIVE,
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
        globalSupportedPlatformTypes = allPlatformsTypes,
        propertyDefinitions = setOf(
            PropertyDefinition(
                name = HOST_PROPERTY_NAME,
                propertyType = PropertyType.STRING,
                nullable = true,
                supportedPlatformTypes = emptySet(),
            ),
            PropertyDefinition(
                name = DOMAIN_PROPERTY_NAME,
                propertyType = PropertyType.STRING,
                nullable = false,
                supportedPlatformTypes = emptySet(),
            ),
            PropertyDefinition(
                name = IS_DEBUG_PROPERTY_NAME,
                propertyType = PropertyType.BOOLEAN,
                nullable = true,
                supportedPlatformTypes = emptySet(),
            ),
            PropertyDefinition(
                name = IS_PRODUCTION_PROPERTY_NAME,
                propertyType = PropertyType.BOOLEAN,
                nullable = false,
                supportedPlatformTypes = emptySet(),
            )
        )
    )

    val schemaWithRestrictedPlatformTypes = Schema(
        globalSupportedPlatformTypes = setOf(
            ANDROID,
            JVM,
        ),
        propertyDefinitions = setOf(
            PropertyDefinition(
                name = HOST_PROPERTY_NAME,
                propertyType = PropertyType.STRING,
                nullable = true,
                supportedPlatformTypes = setOf(ANDROID)
            ),
            PropertyDefinition(
                name = DOMAIN_PROPERTY_NAME,
                propertyType = PropertyType.STRING,
                nullable = false,
                supportedPlatformTypes = emptySet(),
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

    val jsPlatform = Platform(
        platformType = JS,
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

    val nativePlatform = Platform(
        platformType = NATIVE,
        properties = properties
    )

    val environment = Environment(
        name = LOCAL_ENVIRONMENT_NAME,
        platforms = setOf(
            androidPlatform,
            wasmPlatform,
            jsPlatform,
            jvmPlatform,
            nativePlatform,
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