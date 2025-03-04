package io.github.gerardorodriguezdev.chamaleon.core.safeCollections

import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.NonEmptyString.Companion.toUnsafeNonEmptyString
import java.io.File
import java.io.IOException

public class ExistingDirectory private constructor(private val directory: File) {
    public val path: NonEmptyString = directory.path.toUnsafeNonEmptyString()

    public fun existingFile(
        fileName: NonEmptyString,
        createIfNotPresent: Boolean = false,
    ): ExistingFile? {
        val file = File(directory, fileName.value)
        if (createIfNotPresent) {
            try {
                file.createNewFile()
            } catch (_: IOException) {
                return null
            }
        }

        return if (file.isFile && file.exists()) {
            ExistingFile(
                name = fileName,
                path = path,
                readContentDelegate = { file.readText() },
                writeContentDelegate = { file.writeText(it) }
            )
        } else null
    }

    public fun existingDirectories(filter: (directoryName: String) -> Boolean): List<ExistingDirectory> =
        directory
            .walkTopDown()
            .filter { file -> file.isDirectory && file.exists() && filter(file.name) }
            .map { file -> ExistingDirectory(file) }
            .toList()

    public fun existingFiles(filter: (fileName: String) -> Boolean): List<ExistingFile> =
        directory
            .listFiles()
            .filter { file -> file.isFile && file.exists() && filter(file.name) }
            .mapNotNull { file ->
                existingFile(
                    fileName = file.name.toUnsafeNonEmptyString(),
                )
            }
            .toList()

    public companion object {
        public fun NonEmptyString.existingDirectory(
            createIfNotPresent: Boolean = false,
            fileName: NonEmptyString,
        ): ExistingDirectory? {
            val directory = File(fileName.value)

            if (createIfNotPresent) {
                try {
                    if (!directory.exists()) {
                        directory.mkdir()
                    }
                } catch (_: IOException) {
                    return null
                }
            }

            return if (directory.exists() && directory.isDirectory) {
                ExistingDirectory(directory)
            } else null
        }
    }
}