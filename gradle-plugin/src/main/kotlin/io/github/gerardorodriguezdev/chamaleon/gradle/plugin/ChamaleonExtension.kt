package io.github.gerardorodriguezdev.chamaleon.gradle.plugin

import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

interface ChamaleonExtension {
    val selectedEnvironmentName: Property<String?>

    val environments: SetProperty<Environment>
}