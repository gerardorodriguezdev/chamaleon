package io.github.gerardorodriguezdev.chamaleon.gradle.plugin

import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

public interface Extension {
    public val selectedEnvironmentName: Property<String?>
    public val environments: SetProperty<Environment>

    public fun environment(name: String): Environment =
        environments.get().first { environment -> environment.name == name }

    public fun environmentOrNull(name: String): Environment? =
        environments.get().firstOrNull { environment -> environment.name == name }

    public fun selectedEnvironment(): Environment =
        environments.get().first { environment -> environment.name == selectedEnvironmentName.get() }

    public fun selectedEnvironmentOrNull(): Environment? =
        environments.get().firstOrNull { environment -> environment.name == selectedEnvironmentName.get() }
}