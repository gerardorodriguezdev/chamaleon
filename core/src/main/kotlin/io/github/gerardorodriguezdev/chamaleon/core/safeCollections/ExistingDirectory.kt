package io.github.gerardorodriguezdev.chamaleon.core.safeCollections

import java.io.File

public class ExistingDirectory private constructor(public val directory: File) {
    public fun existingFile(fileName: NonEmptyString): ExistingFile? =
        ExistingFile.of(File(directory, fileName.value))

    public fun existingFile(fileName: String): ExistingFile? =
        if (fileName.isEmpty()) null else ExistingFile.of(File(directory, fileName))

    public fun validFile(fileName: NonEmptyString): ValidFile? = ValidFile.of(File(directory, fileName.value))

    public fun validFile(fileName: String): ValidFile? =
        if (fileName.isEmpty()) null else ValidFile.of(File(directory, fileName))

    public fun nonEmptyStringPath(): NonEmptyString = NonEmptyString.of(directory)

    public companion object {
        public fun of(directory: File): ExistingDirectory? =
            if (directory.isDirectory && directory.exists()) ExistingDirectory(directory) else null

        public fun of(directoryPath: NonEmptyString): ExistingDirectory? = of(File(directoryPath.value))

        public fun of(directoryPath: String): ExistingDirectory? =
            if (directoryPath.isEmpty()) null else of(File(directoryPath))
    }
}