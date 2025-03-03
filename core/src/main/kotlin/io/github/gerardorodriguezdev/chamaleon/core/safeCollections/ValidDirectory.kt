package io.github.gerardorodriguezdev.chamaleon.core.safeCollections

import java.io.File

public class ValidDirectory private constructor(public val directory: File) {
    public companion object {
        public fun File.validDirectoryOf(): ValidDirectory? = if (isDirectory) ValidDirectory(this) else null
    }
}