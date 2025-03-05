package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.presentation.testing

import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform.Property
import io.github.gerardorodriguezdev.chamaleon.core.models.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectDeserializationResult

object TestData {
    const val ENVIRONMENTS_DIRECTORY_PATH = "/environments"

    const val LOCAL_ENVIRONMENT_NAME = "local"
    const val PRODUCTION_ENVIRONMENT_NAME = "production"

    const val HOST_PROPERTY_NAME = "host"
    const val LOCAL_HOST = "localhost"
    const val PRODUCTION_HOST = "1.0.0.0"

    val localHostProperty = Property(
        name = HOST_PROPERTY_NAME,
        value = PropertyValue.StringProperty(LOCAL_HOST)
    )

    val productionHostProperty = Property(
        name = HOST_PROPERTY_NAME,
        value = PropertyValue.StringProperty(PRODUCTION_HOST)
    )

    val localEnvironment = Environment(
        name = LOCAL_ENVIRONMENT_NAME,
        platforms = setOf(
            jvmPlatform(properties = setOf(localHostProperty)),
        )
    )

    val productionEnvironment = Environment(
        name = PRODUCTION_ENVIRONMENT_NAME,
        platforms = setOf(
            jvmPlatform(properties = setOf(productionHostProperty)),
        )
    )

    val successProjectDeserializationResult = ProjectDeserializationResult.Success(
        environmentsDirectoryPath = ENVIRONMENTS_DIRECTORY_PATH,
        selectedEnvironmentName = LOCAL_ENVIRONMENT_NAME,
        environments = setOf(
            localEnvironment,
            productionEnvironment,
        ),
    )

    val environmentsNamesList = listOf(
        LOCAL_ENVIRONMENT_NAME,
        PRODUCTION_ENVIRONMENT_NAME,
    )

    private fun jvmPlatform(properties: Set<Property>): Platform =
        Platform(
            platformType = PlatformType.JVM,
            properties = properties
        )
}