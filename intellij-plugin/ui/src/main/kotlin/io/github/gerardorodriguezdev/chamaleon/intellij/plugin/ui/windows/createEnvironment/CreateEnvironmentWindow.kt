package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyValue
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.Verification
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowState.*
import kotlinx.collections.immutable.ImmutableList

@Composable
fun CreateEnvironmentWindow(
    state: CreateEnvironmentWindowState,
    onAction: (action: CreateEnvironmentWindowAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is SetupEnvironmentState ->
            SetupEnvironmentWindow(
                state = state,
                onAction = onAction,
                modifier = modifier,
            )

        is SetupSchemaState ->
            SetupSchemaWindow(
                state = state,
                onAction = onAction,
            )

        is SetupPropertiesState ->
            SetupPropertiesWindow(
                state = state,
                onAction = onAction,
            )
    }
}

sealed interface CreateEnvironmentWindowState {
    data class SetupEnvironmentState(
        val path: String = "",
        val environmentsDirectoryVerification: Verification? = null,

        val environmentName: String = "",
        val environmentNameVerification: EnvironmentNameVerification = EnvironmentNameVerification.VALID,
    ) : CreateEnvironmentWindowState {
        enum class EnvironmentNameVerification {
            VALID,
            IS_EMPTY,
            IS_DUPLICATED;
        }
    }

    data class SetupSchemaState(
        val title: String,
        val supportedPlatforms: ImmutableList<SupportedPlatform>,
        val propertyDefinitions: ImmutableList<PropertyDefinition>,
    ) : CreateEnvironmentWindowState {
        data class SupportedPlatform(
            val isChecked: Boolean,
            val platformType: PlatformType,
        )

        data class PropertyDefinition(
            val name: String,
            val propertyType: PropertyType,
            val nullable: Boolean,
            val supportedPlatforms: ImmutableList<SupportedPlatform>,
        )
    }

    data class SetupPropertiesState(
        val platforms: ImmutableList<Platform>,
    ) : CreateEnvironmentWindowState {
        data class Platform(
            val platformType: PlatformType,
            val properties: ImmutableList<Property>,
        ) {
            data class Property(
                val name: String,
                val value: PropertyValue,
            )
        }

        sealed class PropertyValue {
            data class StringProperty(val value: String) : PropertyValue() {
                override fun toString(): String = value.toString()
            }

            data class BooleanProperty(val value: Boolean) : PropertyValue() {
                override fun toString(): String = value.toString()
            }

            data class NullableBooleanProperty(val value: Boolean?) : PropertyValue() {
                override fun toString(): String = value.toString()
            }
        }
    }
}

sealed interface CreateEnvironmentWindowAction {
    sealed interface SetupEnvironmentAction : CreateEnvironmentWindowAction {
        data object OnSelectEnvironmentPathClicked : SetupEnvironmentAction
        data class OnEnvironmentNameChanged(val newName: String) : SetupEnvironmentAction
    }

    sealed interface SetupSchemaAction : CreateEnvironmentWindowAction {
        data class OnSupportedPlatformChanged(val newPlatformType: PlatformType) : SetupSchemaAction
        data object OnAddPropertyDefinitionClicked : SetupSchemaAction
        data class OnPropertyNameChanged(val index: Int, val newName: String) : SetupSchemaAction
        data class OnPropertyTypeChanged(val index: Int, val newPropertyType: PropertyType) : SetupSchemaAction
        data class OnNullableChanged(val index: Int, val newValue: Boolean) : SetupSchemaAction
        data class OnPropertyDefinitionSupportedPlatformChanged(val index: Int, val newPlatformType: PlatformType) :
            SetupSchemaAction
    }

    sealed interface SetupPropertiesAction : CreateEnvironmentWindowAction {
        data class OnPropertyNameChanged(val index: Int, val newName: String) : SetupPropertiesAction
        data class OnPropertyValueChanged(val index: Int, val newValue: PropertyValue?) : SetupPropertiesAction
    }
}