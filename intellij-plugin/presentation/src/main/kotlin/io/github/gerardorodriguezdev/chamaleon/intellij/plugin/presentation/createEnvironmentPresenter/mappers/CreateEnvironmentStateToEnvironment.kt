package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createEnvironmentPresenter.mappers

import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createEnvironmentPresenter.CreateEnvironmentState

// TODO: Refactor
internal fun CreateEnvironmentState.toEnvironment(): Environment =
    Environment(
        name = environmentName,
        platforms = platforms.map { platform ->
            Platform(
                platformType = platform.platformType,
                properties = platform.properties.map { property ->
                    Platform.Property(
                        name = property.name,
                        value = when (property.value) {
                            is CreateEnvironmentState.Platform.Property.PropertyValue.StringProperty -> {
                                val value = property.value.value
                                if (value.isEmpty()) {
                                    null
                                } else {
                                    PropertyValue.StringProperty(
                                        property.value.value
                                    )
                                }
                            }

                            is CreateEnvironmentState.Platform.Property.PropertyValue.BooleanProperty ->
                                PropertyValue.BooleanProperty(property.value.value)

                            is CreateEnvironmentState.Platform.Property.PropertyValue.NullableBooleanProperty -> null
                        }
                    )
                }.toSet()
            )
        }.toSet()
    )
