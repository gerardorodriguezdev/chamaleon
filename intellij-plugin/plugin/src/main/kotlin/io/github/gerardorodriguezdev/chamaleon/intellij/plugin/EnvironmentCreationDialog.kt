package io.github.gerardorodriguezdev.chamaleon.intellij.plugin

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.toSize
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
import com.intellij.ui.util.minimumHeight
import com.intellij.ui.util.minimumWidth
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectDeserializer
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectDeserializer.Companion.SCHEMA_FILE
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.BaseDialog
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentPresenter
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.CreateEnvironmentState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.handlers.DefaultSetupEnvironmentHandler
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presenters.createEnvironmentPresenter.mappers.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings.BundleStringsProvider
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings.BundleStringsProvider.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.theme.PluginTheme.Theme
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindow
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowState.SetupEnvironmentState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import org.jetbrains.jewel.bridge.JewelComposePanel
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.enableNewSwingCompositing
import java.io.File
import javax.swing.JComponent

internal class EnvironmentCreationDialog(
    project: Project,
    projectDirectory: File,
    private val projectDeserializer: ProjectDeserializer,
) : BaseDialog(dialogTitle = string(StringsKeys.createEnvironment)) {
    private val uiScope = CoroutineScope(Dispatchers.Swing)

    private val presenter = CreateEnvironmentPresenter(
        uiScope = uiScope,
        ioContext = Dispatchers.IO,
        projectDirectory = projectDirectory,
        setupEnvironmentHandler = DefaultSetupEnvironmentHandler(projectDirectory, projectDeserializer),
        onEnvironmentsDirectorySelected = { selectFileDirectory(project) },
        onFinishButtonClicked = { createEnvironmentState ->
            createEnvironmentState.generateEnvironments(project)
        }
    )

    private val createEnvironmentWindowState = mutableStateOf<CreateEnvironmentWindowState>(SetupEnvironmentState())

    init {
        collectState()

        presenter.onAction(CreateEnvironmentAction.SetupEnvironmentAction.OnInit)
    }

    @OptIn(ExperimentalComposeUiApi::class, ExperimentalJewelApi::class)
    override fun createCenterPanel(): JComponent {
        enableNewSwingCompositing()

        return JewelComposePanel {
            with(LocalDensity.current) {
                Theme {
                    CreateEnvironmentWindow(
                        state = createEnvironmentWindowState.value,
                        onAction = { action ->
                            presenter.onAction(action.toCreateEnvironmentAction())
                        },
                        modifier = Modifier.requiredSize(LocalWindowInfo.current.containerSize.toSize().toDpSize())
                    )
                }
            }
        }.apply {
            minimumWidth = DIALOG_MIN_SIZE
            minimumHeight = DIALOG_MIN_SIZE
        }
    }

    private fun collectState() {
        uiScope.launch {
            presenter.stateFlow.collect { createEnvironmentState ->
                createEnvironmentWindowState.value = createEnvironmentState.toWindowState(BundleStringsProvider)

                setDialogButtonsState(createEnvironmentState.toDialogButtonsState())
            }
        }
    }

    override fun onDialogAction(action: DialogAction) {
        presenter.onAction(action.toCreateEnvironmentAction())
    }

    override fun dispose() {
        uiScope.cancel()
        super.dispose()
    }

    private fun selectFileDirectory(project: Project): String? {
        val fileDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
        val selectedDirectory = FileChooser.chooseFile(
            fileDescriptor,
            project,
            null
        )
        return selectedDirectory?.path
    }

    //TODO: Refactor
    //TODO: Manage errors
    //TODO: Review isCanceled
    //TODO: If error cleanup
    private fun CreateEnvironmentState.generateEnvironments(project: Project) {
        project.backgroundTask(
            taskName = string(StringsKeys.generateEnvironment)
        ) { indicator ->
            indicator.fraction = 1.0

            //TODO: More resilient
            val environmentsDirectory = File(project.basePath, environmentsDirectoryPath)
            if (!environmentsDirectory.exists()) {
                environmentsDirectory.mkdirs()
            }

            val schemaFile = File(environmentsDirectory, SCHEMA_FILE)
            val addSchemaResult = projectDeserializer.addSchema(
                schemaFile = schemaFile,
                newSchema = toSchema(),
            )
            if (addSchemaResult is AddSchemaResult.Failure) {
                //TODO: Remove
                println(addSchemaResult)

                project.showFailureNotification()
                indicator.cancel()
                return@backgroundTask
            }
            indicator.fraction = 50.0

            val addEnvironmentsResult = projectDeserializer.addEnvironments(
                environmentsDirectory = environmentsDirectory,
                environments = setOf(toEnvironment())
            )
            if (addEnvironmentsResult is AddEnvironmentsResult.Failure) {
                //TODO: Remove
                println(addEnvironmentsResult)

                project.showFailureNotification()
                indicator.cancel()
            }
            indicator.fraction = 100.0

            project.showSuccessNotification(environmentsDirectory)
        }
    }

    private fun Project.backgroundTask(
        taskName: String,
        task: (indicator: ProgressIndicator) -> Unit
    ) {
        ProgressManager.getInstance().run(
            object : Task.Backgroundable(this, taskName, true) {
                override fun run(indicator: ProgressIndicator) {
                    task(indicator)
                }
            }
        )
    }

    //TODO: Lexemes
    private fun Project.showSuccessNotification(environmentsDirectory: File) {
        showNotification(
            title = "Title",
            message = "Successful",
            type = NotificationType.INFORMATION,
        )

        environmentsDirectory.onEnvironmentsDirectoryChanged()
        //TODO: Notify plugin to update files as well
    }

    //TODO: Lexemes
    //TODO: All errors notified ok
    private fun Project.showFailureNotification() {
        showNotification(
            title = "Title",
            message = "Error environment",
            type = NotificationType.ERROR,
        )
    }

    private fun Project.showNotification(
        title: String,
        message: String,
        type: NotificationType,
    ) {
        val notification = NOTIFICATION_GROUP.createNotification(
            title = title,
            content = message,
            type = type
        )
        Notifications.Bus.notify(notification, this)
    }

    //TODO: Global extension
    private fun File.onEnvironmentsDirectoryChanged() {
        VfsUtil.markDirtyAndRefresh(true, true, true, this)
    }

    private companion object {
        const val DIALOG_MIN_SIZE = 400
    }
}