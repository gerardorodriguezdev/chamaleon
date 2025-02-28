package io.github.gerardorodriguezdev.chamaleon.core.safeCollections

import java.io.File

public class ExistingFile private constructor(public val file: File) {
    public companion object {
        public fun of(file: File): ExistingFile? = if (file.isFile && file.exists()) ExistingFile(file) else null
    }
}