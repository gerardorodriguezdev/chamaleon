package org.chamaleon.gradle.plugin

import org.chamaleon.core.models.Environment
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

interface ChamaleonExtension {
    val selectedEnvironmentName: Property<String?>

    val environments: SetProperty<Environment>
}