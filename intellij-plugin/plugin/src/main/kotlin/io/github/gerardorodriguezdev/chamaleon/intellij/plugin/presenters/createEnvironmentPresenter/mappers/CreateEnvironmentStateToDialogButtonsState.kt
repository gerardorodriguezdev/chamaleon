package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.mappers

import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.BaseDialog.DialogButtonsState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState.*

internal fun CreateEnvironmentState.toDialogButtonsState(): DialogButtonsState =
    DialogButtonsState(
        isPreviousButtonEnabled = isPreviousButtonEnabled(),
        isNextButtonEnabled = isNextButtonEnabled(),
        isFinishButtonEnabled = isFinishButtonEnabled(),
    )

private fun CreateEnvironmentState.isPreviousButtonEnabled(): Boolean =
    when (step) {
        Step.SETUP_ENVIRONMENT -> false
        Step.SETUP_SCHEMA -> true
        Step.SETUP_PROPERTIES -> true
    }

private fun CreateEnvironmentState.isNextButtonEnabled(): Boolean =
    when (step) {
        Step.SETUP_ENVIRONMENT ->
            environmentsDirectoryProcessResult.isValid() && toEnvironmentNameVerification().isValid()

        Step.SETUP_SCHEMA ->
            globalSupportedPlatforms.isNotEmpty() && propertyDefinitions.areValid(globalSupportedPlatforms)

        Step.SETUP_PROPERTIES -> false
    }

private fun Set<PropertyDefinition>.areValid(globalSupportedPlatforms: Set<PlatformType>): Boolean =
    !any {
        !isNotEmpty() &&
                !areUnique() &&
                !any { propertyDefinition -> !propertyDefinition.isValid(globalSupportedPlatforms) }
    }

private fun Set<PropertyDefinition>.areUnique(): Boolean =
    distinctBy { propertyDefinition -> propertyDefinition.name }.size == size

private fun PropertyDefinition.isValid(globalSupportedPlatforms: Set<PlatformType>): Boolean =
    name.isNotEmpty() &&
            supportedPlatforms.isNotEmpty() &&
            globalSupportedPlatforms.containsAll(supportedPlatforms)

private fun CreateEnvironmentState.isFinishButtonEnabled(): Boolean =
    when (step) {
        Step.SETUP_ENVIRONMENT -> false
        Step.SETUP_SCHEMA -> false
        Step.SETUP_PROPERTIES -> platforms.arePlatformsValid(propertyDefinitions)
    }

private fun Set<Platform>.arePlatformsValid(propertyDefinitions: Set<PropertyDefinition>): Boolean =
    !any { platform ->
        !platform.properties.areUnique() &&
                !platform.properties.areValuesValid(propertyDefinitions)
    }

private fun Set<Platform.Property>.areValuesValid(propertyDefinitions: Set<PropertyDefinition>): Boolean =
    any { property -> !property.isValueValid(propertyDefinitions) }

private fun Platform.Property.isValueValid(propertyDefinitions: Set<PropertyDefinition>): Boolean =
    when (value) {
        is Platform.Property.PropertyValue.StringProperty -> {
            val propertyDefinition = propertyDefinitions.propertyDefinition(name)
            if (propertyDefinition.nullable) true else value.value.isNotEmpty()
        }

        is Platform.Property.PropertyValue.BooleanProperty -> true
        is Platform.Property.PropertyValue.NullableBooleanProperty -> true
    }

private fun Set<PropertyDefinition>.propertyDefinition(name: String): PropertyDefinition =
    first { propertyDefinition ->
        propertyDefinition.name == name
    }

private fun Set<Platform.Property>.areUnique(): Boolean =
    distinctBy { property -> property.name }.size == size

private fun EnvironmentsDirectoryProcessResult.isValid(): Boolean = this is EnvironmentsDirectoryProcessResult.Success