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
import com.intellij.ui.util.minimumHeight
import com.intellij.ui.util.minimumWidth
import io.github.gerardorodriguezdev.chamaleon.core.models.Project.Companion.SCHEMA_FILE
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectDeserializer
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.BaseDialog
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.CreateProjectAction.SetupEnvironmentAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.CreateProjectPresenter
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings.BundleStringsProvider
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.strings.BundleStringsProvider.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.theme.PluginTheme.Theme
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindow
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowState.SetupEnvironmentState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import org.jetbrains.jewel.bridge.JewelComposePanel
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.enableNewSwingCompositing
import javax.swing.JComponent
import io.github.gerardorodriguezdev.chamaleon.core.models.Project as ChamaleonProject

internal class CreateProjectDialog(
    project: Project,
    projectDirectory: ExistingDirectory,
    private val projectDeserializer: ProjectDeserializer,
) : BaseDialog(dialogTitle = string(StringsKeys.createEnvironment)) {
    private val uiScope = CoroutineScope(Dispatchers.Swing)

    private val presenter = CreateProjectPresenter(
        uiScope = uiScope,
        uiContext = Dispatchers.Swing,
        ioContext = Dispatchers.IO,
        projectDeserializer = projectDeserializer,
        onFinish = { chamaleonProject ->
            project.generateEnvironments(chamaleonProject)
        }
    )

    private val createProjectWindowState = mutableStateOf<CreateProjectWindowState>(SetupEnvironmentState())

    init {
        collectState()

        presenter.dispatch(SetupEnvironmentAction.OnEnvironmentsDirectoryChanged(projectDirectory))
    }

    @OptIn(ExperimentalComposeUiApi::class, ExperimentalJewelApi::class)
    override fun createCenterPanel(): JComponent {
        enableNewSwingCompositing()

        return JewelComposePanel {
            with(LocalDensity.current) {
                Theme {
                    CreateProjectWindow(
                        state = createProjectWindowState.value,
                        onAction = { action ->
                            presenter.dispatch(action.toCreateEnvironmentAction())
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
                createProjectWindowState.value = createEnvironmentState.toWindowState(BundleStringsProvider)

                setDialogButtonsState(createEnvironmentState.toDialogButtonsState())
            }
        }
    }

    override fun onDialogAction(action: DialogAction) {
        presenter.dispatch(action.toCreateEnvironmentAction())
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

    // TODO: Refactor
    // TODO: Manage errors
    // TODO: Review isCanceled
    // TODO: If error cleanup
    private fun Project.generateEnvironments(project: ChamaleonProject) {
        backgroundTask(taskName = string(StringsKeys.generateEnvironment)) { indicator ->
            indicator.fraction = 1.0

            // TODO: More resilient
            val environmentsDirectory = File(basePath, project.environmentsDirectory.path.value)
            if (!environmentsDirectory.exists()) {
                environmentsDirectory.mkdirs()
            }

            val schemaFile = File(environmentsDirectory, SCHEMA_FILE)
            val addSchemaResult = projectDeserializer.addSchema(
                schemaFile = schemaFile,
                newSchema = toSchema(),
            )
            if (addSchemaResult is AddSchemaResult.Failure) {
                // TODO: Remove
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
                // TODO: Remove
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

    // TODO: Lexemes
    private fun Project.showSuccessNotification(environmentsDirectory: File) {
        showNotification(
            title = "Title",
            message = "Successful",
            type = NotificationType.INFORMATION,
        )

        environmentsDirectory.onEnvironmentsDirectoryChanged()
        // TODO: Notify plugin to update files as well
    }

    // TODO: Lexemes
    // TODO: All errors notified ok
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
        @Suppress("UnstableApiUsage")
        val notification = NOTIFICATION_GROUP.createNotification(
            title = title,
            content = message,
            type = type
        )
        Notifications.Bus.notify(notification, this)
    }

    private companion object {
        const val DIALOG_MIN_SIZE = 400
    }
}