package de.berlindroid.zeapp.zevm

import de.berlindroid.zeapp.zeservices.ZeBadgeManager
import de.berlindroid.zeapp.zeservices.ZeClipboardService
import de.berlindroid.zeapp.zeservices.ZeImageProviderService
import de.berlindroid.zeapp.zeservices.ZePreferencesService
import de.berlindroid.zeapp.zeservices.ZeWeatherService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ZeBadgeViewModelTest {

    private val imageProviderService = mockk<ZeImageProviderService>()
    private val zeBadgeManager = mockk<ZeBadgeManager>()
    private val zePreferencesService = mockk<ZePreferencesService>()
    private val clipboardService = mockk<ZeClipboardService>()
    private val weatherService = mockk<ZeWeatherService>()
    private val templateConfigurations = mockk<GetTemplateConfigurations>()
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
    fun testCopyToClipBoard() {
        // Given
        every { clipboardService.copyToClipboard("") } returns Unit
        val zeBadgeViewModel = provideViewModel()

        // When
        zeBadgeViewModel.copyInfoToClipboard()

        // Then
        verify { clipboardService.copyToClipboard("") }
    }

    private fun provideViewModel() = ZeBadgeViewModel(
        imageProviderService = imageProviderService,
        badgeManager = zeBadgeManager,
        preferencesService = zePreferencesService,
        clipboardService = clipboardService,
        weatherService = weatherService,
        getTemplateConfigurations = templateConfigurations,
    )

    @After
    fun clearMainDispatcher() {
        Dispatchers.resetMain()
    }
}
