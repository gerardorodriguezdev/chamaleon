package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.mappers

import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState

//TODO: Refactor
internal fun CreateEnvironmentState.toSchema(): Schema =
    Schema(
        globalSupportedPlatformTypes = globalSupportedPlatforms,
        propertyDefinitions = propertyDefinitions.map { propertyDefinition ->
            Schema.PropertyDefinition(
                name = propertyDefinition.name,
                propertyType = propertyDefinition.propertyType,
                nullable = propertyDefinition.nullable,
                supportedPlatformTypes = propertyDefinition.supportedPlatforms,
            )
        }.toSet()
    )