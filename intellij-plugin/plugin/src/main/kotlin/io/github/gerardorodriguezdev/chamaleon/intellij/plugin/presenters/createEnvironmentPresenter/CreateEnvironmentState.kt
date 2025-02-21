package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter

import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyType

internal data class CreateEnvironmentState(
    val environmentsDirectoryPath: String = "",
    val environmentsDirectoryProcessResult: EnvironmentsDirectoryProcessResult =
        EnvironmentsDirectoryProcessResult.Loading,
    val environmentName: String = "",
    val environmentsNames: Set<String> = emptySet(),

    val globalSupportedPlatforms: Set<PlatformType> = emptySet(),
    val propertyDefinitions: Set<PropertyDefinition> = emptySet(),

    val platforms: Set<Platform> = emptySet(),
    val step: Step = Step.SETUP_ENVIRONMENT,
) {
    sealed interface EnvironmentsDirectoryProcessResult {
        data object Success : EnvironmentsDirectoryProcessResult
        data object Loading : EnvironmentsDirectoryProcessResult
        sealed interface Failure : EnvironmentsDirectoryProcessResult {
            data object EnvironmentsDirectoryNotFound : Failure
            data object SchemaFileNotFound : Failure
            data object FileIsNotDirectory : Failure
            data object InvalidEnvironments : Failure
        }
    }

    data class PropertyDefinition(
        val name: String,
        val propertyType: PropertyType,
        val nullable: Boolean,
        val supportedPlatforms: Set<PlatformType>,
    )

    data class Platform(
        val platformType: PlatformType,
        val properties: Set<Property>,
    ) {
        data class Property(
            val name: String,
            val value: PropertyValue,
        ) {
            sealed interface PropertyValue {
                data class StringProperty(val value: String) : PropertyValue
                data class BooleanProperty(val value: Boolean) : PropertyValue
                data class NullableBooleanProperty(val value: Boolean?) : PropertyValue
            }
        }
    }

    enum class Step {
        SETUP_ENVIRONMENT,
        SETUP_SCHEMA,
        SETUP_PROPERTIES,
    }
}