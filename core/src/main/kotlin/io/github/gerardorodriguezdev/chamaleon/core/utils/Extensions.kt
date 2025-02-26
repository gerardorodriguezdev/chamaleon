package io.github.gerardorodriguezdev.chamaleon.core.utils

internal fun <T> Set<T>.containsDuplicates(selector: (T) -> String): Boolean = distinctBy(selector).size != size