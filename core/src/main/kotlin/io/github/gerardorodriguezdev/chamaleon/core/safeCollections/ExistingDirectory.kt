package io.github.gerardorodriguezdev.chamaleon.core.safeCollections

import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.ExistingFile.Companion.toExistingFile
import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.NonEmptyString.Companion.toUnsafeNonEmptyString
import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.ValidFile.Companion.toValidFile
import java.io.File

public class ExistingDirectory private constructor(public val directory: File) {
    public fun existingFile(fileName: NonEmptyString): ExistingFile? =
        File(directory, fileName.value).toExistingFile()

    public fun existingFile(fileName: String): ExistingFile? =
        if (fileName.isEmpty()) null else File(directory, fileName).toExistingFile()

    public fun validFile(fileName: NonEmptyString): ValidFile? = File(directory, fileName.value).toValidFile()

    public fun validFile(fileName: String): ValidFile? =
        if (fileName.isEmpty()) null else File(directory, fileName).toValidFile()

    public fun nonEmptyStringPath(): NonEmptyString = directory.path.toUnsafeNonEmptyString()

    public companion object {
        public fun File.toUnsafeExistingDirectory(): ExistingDirectory =
            if (!isDirectory || !exists()) {
                throw IllegalStateException("Existing directory invalid")
            } else {
                ExistingDirectory(this)
            }

        public fun File.toExistingDirectory(): ExistingDirectory? =
            if (isDirectory && exists()) ExistingDirectory(this) else null

        public fun NonEmptyString.toExistingDirectory(): ExistingDirectory? = File(value).toExistingDirectory()

        public fun String.toExistingDirectory(): ExistingDirectory? =
            if (isEmpty()) null else File(this).toExistingDirectory()
    }
}