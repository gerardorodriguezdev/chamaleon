package io.github.gerardorodriguezdev.chamaleon.core.safeCollections

import java.io.File

public class ExistingFile private constructor(public val file: File) {
    public companion object {
        public fun File.toExistingFile(): ExistingFile? = if (isFile && exists()) ExistingFile(this) else null
    }
}