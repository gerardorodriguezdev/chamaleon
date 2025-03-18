package io.github.gerardorodriguezdev.chamaleon.core.utils

public fun <T> List<T>.updateElementByIndex(index: Int, block: (T) -> T): List<T> =
    mapIndexed { currentIndex, element ->
        if (currentIndex == index) {
            block(element)
        } else {
            element
        }
    }

public fun <T> List<T>.removeElementAtIndex(index: Int): List<T> =
    mapIndexedNotNull { currentIndex, element ->
        if (currentIndex == index) {
            null
        } else {
            element
        }
    }