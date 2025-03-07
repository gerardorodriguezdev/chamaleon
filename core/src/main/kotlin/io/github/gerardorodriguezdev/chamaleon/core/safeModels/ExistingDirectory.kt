package io.github.gerardorodriguezdev.chamaleon.core.safeModels

import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingFile.Companion.isExistingFile
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingFile.Companion.toExistingFile
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString.Companion.toUnsafeNonEmptyString
import java.io.File
import java.io.IOException

public class ExistingDirectory private constructor(private val directory: File) {
    public val path: NonEmptyString = directory.path.toUnsafeNonEmptyString()

    init {
        if (!directory.isExistingDirectory()) {
            throw IllegalArgumentException("Directory invalid ${directory.path}")
        }
    }

    public fun existingFile(
        fileName: NonEmptyString,
        createIfNotPresent: Boolean = false,
    ): ExistingFile? =
        File(directory, fileName.value).toExistingFile(createIfNotPresent)

    public fun existingFiles(filter: (fileName: String) -> Boolean): List<ExistingFile> =
        directory
            .listFiles()
            .filter { file -> file.isExistingFile() && filter(file.name) }
            .mapNotNull { file ->
                existingFile(fileName = file.name.toUnsafeNonEmptyString())
            }
            .toList()

    public fun existingDirectories(filter: (directoryName: String) -> Boolean): List<ExistingDirectory> =
        directory
            .walkTopDown()
            .filter { file -> file.isExistingDirectory() && filter(file.name) }
            .map { file -> ExistingDirectory(file) }
            .toList()

    public companion object {
        private fun File.isExistingDirectory(): Boolean = exists() && isDirectory

        public fun File.toUnsafeExistingDirectory(): ExistingDirectory = ExistingDirectory(this)

        public fun File.toExistingDirectory(createIfNotPresent: Boolean = false): ExistingDirectory? {
            val directory = File(path)

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