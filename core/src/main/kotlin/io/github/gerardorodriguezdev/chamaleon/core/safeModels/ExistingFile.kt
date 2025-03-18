package io.github.gerardorodriguezdev.chamaleon.core.safeModels

import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString.Companion.toUnsafeNonEmptyString
import java.io.File
import java.io.IOException

@Suppress("UseRequire")
public class ExistingFile private constructor(file: File) {
    init {
        if (!file.isExistingFile()) throw IllegalArgumentException("File invalid ${file.path}")
    }

    public val name: NonEmptyString = file.name.toUnsafeNonEmptyString()
    public val path: NonEmptyString = file.path.toUnsafeNonEmptyString()
    private val readContentDelegate: () -> String = { file.readText() }
    private val writeContentDelegate: (String) -> Unit = { content -> file.writeText(content) }

    public fun readContent(): String = readContentDelegate()

    public fun writeContent(content: String) {
        writeContentDelegate(content)
    }

    internal companion object {
        internal fun File.isExistingFile(): Boolean = exists() && isFile

        fun File.toExistingFile(createIfNotPresent: Boolean = false): ExistingFile? {
            if (createIfNotPresent) {
                try {
                    if (!exists()) {
                        createNewFile()
                    }
                } catch (_: IOException) {
                    return null
                }
            }

            return if (isExistingFile()) ExistingFile(this) else null
        }
    }
}