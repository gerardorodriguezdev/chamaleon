package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.models.Field
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowState.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowState.SetupPlatformsState.Platform.Property.PropertyValue
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowState.SetupSchemaState.PropertyDefinition.PropertyType
import kotlinx.collections.immutable.ImmutableList

@Composable
fun CreateProjectWindow(
    state: CreateProjectWindowState,
    onAction: (action: CreateProjectWindowAction) -> Unit,
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

        is SetupPlatformsState ->
            SetupPlatformsWindow(
                state = state,
                onAction = onAction,
            )
    }
}

sealed interface CreateProjectWindowState {
    data class SetupEnvironmentState(
        val environmentsDirectoryPathField: Field<String> = Field(value = ""),
        val environmentNameField: Field<String> = Field(value = ""),
    ) : CreateProjectWindowState

    data class SetupSchemaState(
        val title: String,
        val globalSupportedPlatforms: ImmutableList<PlatformType>,
        val propertyDefinitions: ImmutableList<PropertyDefinition>,
    ) : CreateProjectWindowState {
        data class PropertyDefinition(
            val nameField: Field<String>,
            val propertyType: PropertyType,
            val nullable: Boolean,
            val supportedPlatforms: ImmutableList<PlatformType>,
        ) {
            enum class PropertyType {
                STRING,
                BOOLEAN,
            }
        }
    }

    data class SetupPlatformsState(
        val platforms: ImmutableList<Platform>,
    ) : CreateProjectWindowState {
        data class Platform(
            val platformType: PlatformType,
            val properties: ImmutableList<Property>,
        ) {
            enum class PlatformType(val serialName: String) {
                ANDROID("android"),
                WASM("wasm"),
                JS("js"),
                NATIVE("native"),
                JVM("jvm")
            }

            data class Property(
                val name: String,
                val value: PropertyValue,
            ) {
                sealed class PropertyValue {
                    data class StringProperty(val valueField: Field<String>) : PropertyValue() {
                        override fun toString(): String = valueField.value.toString()
                    }

                    data class NullableStringProperty(val value: String) : PropertyValue() {
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
    }
}

sealed interface CreateProjectWindowAction {
    sealed interface SetupEnvironmentAction : CreateProjectWindowAction {
        data object OnSelectEnvironmentPath : SetupEnvironmentAction
        data class OnEnvironmentNameChanged(val newName: String) : SetupEnvironmentAction
    }

    sealed interface SetupSchemaAction : CreateProjectWindowAction {
        data class OnGlobalSupportedPlatformTypesChanged(
            val isChecked: Boolean,
            val newPlatformType: PlatformType,
        ) : SetupSchemaAction

        data object OnAddPropertyDefinition : SetupSchemaAction
        data class OnDeletePropertyDefinition(val index: Int) : SetupSchemaAction
        data class OnPropertyDefinitionNameChanged(val index: Int, val newName: String) : SetupSchemaAction
        data class OnPropertyDefinitionTypeChanged(val index: Int, val newPropertyType: PropertyType) :
            SetupSchemaAction

        data class OnNullableChanged(val index: Int, val newValue: Boolean) : SetupSchemaAction
        data class OnSupportedPlatformTypesChanged(
            val index: Int,
            val isChecked: Boolean,
            val newPlatformType: PlatformType
        ) : SetupSchemaAction
    }

    sealed interface SetupPlatformsAction : CreateProjectWindowAction {
        data class OnPropertyValueChanged(
            val platformType: PlatformType,
            val index: Int,
            val newValue: PropertyValue,
        ) : SetupPlatformsAction
    }
}