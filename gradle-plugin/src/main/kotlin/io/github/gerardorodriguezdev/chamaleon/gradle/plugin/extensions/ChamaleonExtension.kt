package io.github.gerardorodriguezdev.chamaleon.gradle.plugin.extensions

import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.models.Project
import org.gradle.api.provider.Property

public interface ChamaleonExtension {
    public val project: Property<Project>

    public fun environment(name: String): Environment = project.get().environments!!.getValue(name)

    public fun environmentOrNull(name: String): Environment? = project.get().environments?.get(name)

    public fun selectedEnvironment(): Environment = project.get().selectedEnvironment()!!

    public fun selectedEnvironmentOrNull(): Environment? = project.get().selectedEnvironment()
}