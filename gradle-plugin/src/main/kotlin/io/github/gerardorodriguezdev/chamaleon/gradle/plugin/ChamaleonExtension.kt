package io.github.gerardorodriguezdev.chamaleon.gradle.plugin

import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

public interface ChamaleonExtension {
    public val selectedEnvironmentName: Property<String?>
    public val environments: SetProperty<Environment>
}