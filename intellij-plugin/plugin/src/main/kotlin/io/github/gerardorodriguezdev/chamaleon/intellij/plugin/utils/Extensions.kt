package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.utils

import com.intellij.codeInspection.ex.GlobalInspectionContextImpl.NOTIFICATION_GROUP
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

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

internal fun Project.runBackgroundTask(
    taskName: String,
    task: suspend (progressIndicator: ProgressIndicator) -> Unit
) {
    ProgressManager.getInstance().run(
        object : Task.Backgroundable(this, taskName, true) {
            private val taskScope = CoroutineScope(Dispatchers.IO)

            override fun run(indicator: ProgressIndicator) {
                taskScope.launch {
                    task(indicator)
                }
            }

            override fun onFinished() {
                super.onFinished()

                taskScope.cancel()
            }
        }
    )
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