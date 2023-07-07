package de.berlindroid.zeapp.zevm

import android.graphics.Bitmap
import de.berlindroid.zeapp.zebits.isBinary
import de.berlindroid.zeapp.zemodels.ZeConfiguration
import de.berlindroid.zeapp.zemodels.ZeSlot
import de.berlindroid.zeapp.zeservices.ZeBadgeUploader
import de.berlindroid.zeapp.zeservices.ZeClipboardService
import de.berlindroid.zeapp.zeservices.ZeContributorsService
import de.berlindroid.zeapp.zeservices.ZeImageProviderService
import de.berlindroid.zeapp.zeservices.ZePreferencesService
import io.mockk.called
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
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

    val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setupMainDispatcher() {
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun testSendPageToDeviceSlotNotFound() {
        // Given
        every { contributorsService.contributors() } returns emptyFlow()
        val zeBadgeViewModel = provideViewModel()
        val bitmap = mockk<Bitmap>()
        mockkStatic("de.berlindroid.zeapp.zebits.ZeBitmapManipulationKt")
        every { bitmap.isBinary() } returns false

        val slot = ZeSlot.BarCode
        zeBadgeViewModel.slots.value =
            mapOf(slot to ZeConfiguration.Name("Droidcon", "Berlin", bitmap = bitmap))

        // When
        zeBadgeViewModel.sendPageToDevice(slot)

        coVerify(exactly = 0) { zeBadgeUploader.sendPage("name", bitmap) }
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
        contributorsService = contributorsService
    )

    @After
    fun clearMainDispatcher() {
        Dispatchers.resetMain()
    }
}
