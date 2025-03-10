package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.utils

import com.intellij.openapi.vfs.VfsUtil
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory

internal fun ExistingDirectory.onEnvironmentsDirectoryChanged() {
    VfsUtil.markDirtyAndRefresh(true, true, true, directory)
}