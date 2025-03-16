package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.utils

import com.intellij.codeInspection.ex.GlobalInspectionContextImpl.NOTIFICATION_GROUP
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory.Companion.toExistingDirectory

internal fun ExistingDirectory.notifyDirectoryChanged() {
    VfsUtil.markDirtyAndRefresh(true, true, true, directory)
}

internal fun Project.selectFileDirectoryPath(): String? {
    val fileDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
    val selectedDirectory = FileChooser.chooseFile(
        fileDescriptor,
        this,
        null
    )
    return selectedDirectory?.path
}

internal fun Project.showSuccessNotification(
    title: String,
    message: String,
) {
    showNotification(
        title = title,
        message = message,
        type = NotificationType.INFORMATION,
    )
}

internal fun Project.showFailureNotification(
    title: String,
    message: String,
) {
    showNotification(
        title = title,
        message = message,
        type = NotificationType.ERROR,
    )
}

private fun Project.showNotification(
    title: String,
    message: String,
    type: NotificationType,
) {
    @Suppress("UnstableApiUsage")
    val notification = NOTIFICATION_GROUP.createNotification(
        title = title,
        content = message,
        type = type
    )
    Notifications.Bus.notify(notification, this)
}

internal fun Project.toExistingDirectory(): ExistingDirectory? = basePath?.toExistingDirectory()