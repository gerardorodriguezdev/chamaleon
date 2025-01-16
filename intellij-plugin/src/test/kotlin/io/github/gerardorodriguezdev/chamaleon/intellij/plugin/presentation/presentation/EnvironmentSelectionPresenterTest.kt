package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.presentation

import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.EnvironmentSelectionPresenter
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.presentation.testing.FakeEnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.presentation.testing.TestData
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.EnvironmentCardState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.EnvironmentSelectionState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class EnvironmentSelectionPresenterTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var projectDirectory: File
    private var environmentDirectoryChangedEvents = mutableListOf<File>()
    private val environmentsProcessor = FakeEnvironmentsProcessor()
    private val presenter = EnvironmentSelectionPresenter(
        environmentsProcessor = environmentsProcessor,
        uiDispatcher = dispatcher,
        ioDispatcher = dispatcher,
        onEnvironmentsDirectoryChanged = { file ->
            environmentDirectoryChangedEvents.add(file)
        }
    )

    @BeforeTest
    fun setup() {
        projectDirectory = createTempDirectory().toFile()
        val environmentsDirectory = File(projectDirectory, TestData.ENVIRONMENTS_PATH)
        environmentsDirectory.mkdirs()
    }

    @AfterTest
    fun teardown() {
        projectDirectory.deleteRecursively()
    }

    @Test
    fun `WHEN presenter is created THEN returns empty state`() {
        assertEquals(
            expected = emptyState,
            actual = presenter.state.value,
        )
    }

    @Test
    fun `GIVEN invalid environments directory WHEN scan project THEN nothing happens`() {
        environmentsProcessor.processRecursivelyResult = emptyList()

        presenter.scanProject(projectDirectory)

        assertEquals(
            expected = emptyState,
            actual = presenter.state.value,
        )
    }

    @Test
    fun `GIVEN valid environments directory WHEN scan project THEN returns complete state`() {
        presenter.scanProject(projectDirectory)

        assertEquals(
            expected = environmentSelectionState(),
            actual = presenter.state.value,
        )
    }

    @Test
    fun `GIVEN invalid environment WHEN onSelectEnvironmentChanged THEN nothing happens`() {
        environmentsProcessor.updateSelectedEnvironmentResult = false
        presenter.scanProject(projectDirectory)

        presenter.onSelectedEnvironmentChanged(
            projectDirectory = projectDirectory,
            environmentsDirectoryPath = TestData.ENVIRONMENTS_PATH,
            newSelectedEnvironment = "NonExistingEnvironment"
        )

        assertEquals(
            expected = environmentSelectionState(),
            actual = presenter.state.value,
        )
        assertEquals(
            expected = emptyList(),
            actual = environmentDirectoryChangedEvents,
        )
    }

    @Test
    fun `GIVEN valid environment WHEN onSelectEnvironmentChanged THEN updates selected environment`() {
        val expectedUpdatedEnvironmentsDirectory = File(projectDirectory, TestData.ENVIRONMENTS_PATH)
        presenter.scanProject(projectDirectory)

        presenter.onSelectedEnvironmentChanged(
            projectDirectory = projectDirectory,
            environmentsDirectoryPath = TestData.ENVIRONMENTS_PATH,
            newSelectedEnvironment = TestData.PRODUCTION_ENVIRONMENT_NAME
        )

        assertEquals(
            expected = environmentSelectionState(
                environmentCardStates = listOf(
                    environmentCardState(
                        selectedEnvironment = TestData.PRODUCTION_ENVIRONMENT_NAME,
                    )
                )
            ),
            actual = presenter.state.value,
        )
        assertEquals(
            expected = listOf(expectedUpdatedEnvironmentsDirectory),
            actual = environmentDirectoryChangedEvents,
        )
    }

    companion object {
        private val emptyState = environmentSelectionState(
            environmentCardStates = persistentListOf(),
        )

        private fun environmentSelectionState(
            environmentCardStates: List<EnvironmentCardState> = listOf(environmentCardState()),
            isLoading: Boolean = false,
        ): EnvironmentSelectionState =
            EnvironmentSelectionState(
                environmentCardStates = environmentCardStates.toPersistentList(),
                isLoading = isLoading,
            )

        private fun environmentCardState(
            environmentsDirectoryPath: String = TestData.ENVIRONMENTS_PATH,
            selectedEnvironment: String = TestData.LOCAL_ENVIRONMENT_NAME,
            environments: List<String> = TestData.environmentsNamesList,
        ): EnvironmentCardState =
            EnvironmentCardState(
                environmentsDirectoryPath = environmentsDirectoryPath,
                selectedEnvironment = selectedEnvironment,
                environments = environments.toPersistentList(),
            )
    }
}