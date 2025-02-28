package io.github.gerardorodriguezdev.chamaleon.core.safeCollections

import java.io.File

public class ValidDirectory private constructor(public val directory: File) {
    public companion object {
        public fun of(directory: File): ValidDirectory? =
            if (directory.isDirectory) ValidDirectory(directory) else null
    }
}