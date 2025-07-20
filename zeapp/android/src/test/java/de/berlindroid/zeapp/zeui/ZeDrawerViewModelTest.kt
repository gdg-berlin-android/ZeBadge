package de.berlindroid.zeapp.zeui

import de.berlindroid.zeapp.zeservices.github.ZeReleaseService
import de.berlindroid.zeapp.zeui.zehome.DrawerItemsProvider
import de.berlindroid.zeapp.zeui.zehome.ZeDrawerViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ZeDrawerViewModelTest {
    private val releaseService = mockk<ZeReleaseService>()
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setupMainDispatcher() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has no new release version`() =
        runTest {
            // Given
            coEvery { releaseService.getNewRelease() } returns null
            val viewModel = provideViewModel()

            // When
            val uiState = viewModel.uiState.first()

            // Then
            assertNull(uiState.newReleaseVersion)
            assertEquals(DrawerItemsProvider.getDrawerItems(), uiState.drawerItems)
        }

    @Test
    fun `checkForNewRelease updates state when new version available`() =
        runTest {
            // Given
            val expectedVersion = 42
            coEvery { releaseService.getNewRelease() } returns expectedVersion
            val viewModel = provideViewModel()

            // When
            val uiState = viewModel.uiState.first()

            // Then
            assertEquals(expectedVersion, uiState.newReleaseVersion)
        }

    @Test
    fun `checkForNewRelease handles null response correctly`() =
        runTest {
            // Given
            coEvery { releaseService.getNewRelease() } returns null
            val viewModel = provideViewModel()

            // When
            val uiState = viewModel.uiState.first()

            // Then
            assertNull(uiState.newReleaseVersion)
        }

    private fun provideViewModel() = ZeDrawerViewModel(releaseService = releaseService)
}
