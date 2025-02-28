package io.github.gerardorodriguezdev.chamaleon.core.safeCollections

import java.io.File

public class ValidFile private constructor(private val file: File) {
    public val path: String = file.path

    public fun toExistingFile(): ExistingFile? = ExistingFile.of(file)

    public fun toSafeExistingFile(): ExistingFile {
        if (!file.exists()) {
            file.createNewFile()
        }

        return requireNotNull(ExistingFile.of(file))
    }

    public companion object {
        public fun of(file: File): ValidFile? = if (file.isFile) ValidFile(file) else null
    }
}