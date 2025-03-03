package io.github.gerardorodriguezdev.chamaleon.core.safeCollections

import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.ExistingFile.Companion.toExistingFile
import java.io.File
import java.io.IOException

public class ValidFile private constructor(private val file: File) {
    public val path: String = file.path

    public fun toExistingFile(createIfNotPresent: Boolean = false): ExistingFile? {
        if (createIfNotPresent) {
            try {
                if (!file.exists()) {
                    file.createNewFile()
                }
            } catch (_: IOException) {
                null
            }
        }

        return file.toExistingFile()
    }

    public companion object {
        public fun File.toValidFile(): ValidFile? = if (isFile) ValidFile(this) else null
    }
}