package de.berlindroid.zeapp.zevm

import de.berlindroid.zeapp.zeservices.ZeBadgeUploader
import de.berlindroid.zeapp.zeservices.ZeClipboardService
import de.berlindroid.zeapp.zeservices.ZeContributorsService
import de.berlindroid.zeapp.zeservices.ZeImageProviderService
import de.berlindroid.zeapp.zeservices.ZePreferencesService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ZeBadgeViewModelTest {

    val imageProviderService = mockk<ZeImageProviderService>()
    val zeBadgeUploader = mockk<ZeBadgeUploader>()
    val zePreferencesService = mockk<ZePreferencesService>()
    val clipboardService = mockk<ZeClipboardService>()
    val contributorsService = mockk<ZeContributorsService>()
    val templateConfigurations = mockk<GetTemplateConfigurations>()

    val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setupMainDispatcher() {
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun testCopyToClipBoard() {
        // Given
        every { contributorsService.contributors() } returns emptyFlow()
        every { clipboardService.copyToClipboard("") } returns Unit
        val zeBadgeViewModel = provideViewModel()

        //When
        zeBadgeViewModel.copyInfoToClipboard()

        // Then
        verify { clipboardService.copyToClipboard("") }
    }

    private fun provideViewModel() = ZeBadgeViewModel(
        imageProviderService = imageProviderService,
        badgeUploader = zeBadgeUploader,
        preferencesService = zePreferencesService,
        clipboardService = clipboardService,
        getTemplateConfigurations = templateConfigurations,
        contributorsService = contributorsService
    )

    @After
    fun clearMainDispatcher() {
        Dispatchers.resetMain()
    }
}
