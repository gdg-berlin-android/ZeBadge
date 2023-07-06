package de.berlindroid.zeapp.zevm

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.berlindroid.zeapp.OPENAI_API_KEY
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.zebits.ditherFloydSteinberg
import de.berlindroid.zeapp.zebits.isBinary
import de.berlindroid.zeapp.zemodels.ZeConfiguration
import de.berlindroid.zeapp.zemodels.ZeEditor
import de.berlindroid.zeapp.zemodels.ZeSlot
import de.berlindroid.zeapp.zemodels.ZeTemplateChooser
import de.berlindroid.zeapp.zeservices.ZeBadgeUploader
import de.berlindroid.zeapp.zeservices.ZeClipboardService
import de.berlindroid.zeapp.zeservices.ZeContributorsService
import de.berlindroid.zeapp.zeservices.ZeImageProviderService
import de.berlindroid.zeapp.zeservices.ZePreferencesService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val MESSAGE_DISPLAY_DURATION = 3_000L
private const val MESSAGE_DISPLAY_UPDATES = 5

/**
 * Base ViewModel building a list of pages for the badge and offering simulator support.
 */
@HiltViewModel
class ZeBadgeViewModel @Inject constructor(
    private val imageProviderService: ZeImageProviderService,
    private val badgeUploader: ZeBadgeUploader,
    private val preferencesService: ZePreferencesService,
    private val clipboardService: ZeClipboardService,
    contributorsService: ZeContributorsService,
) : ViewModel() {

    val snackbarHostState = SnackbarHostState()

    fun showSnackBar(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Long,
    ) {
        viewModelScope.launch {
            snackbarHostState.showSnackbar(message = message, duration = duration)
        }
    }

    // See if disappearing message is ongoing
    private var hideMessageJob: Job? = null
    private var messageProgressJob: Job? = null

    private fun badgeFailure(error: String) {
        message.value = "❗$error ❗️"
        scheduleMessageDisappearance()
    }

    private fun badgeSuccess(bytesSent: Int) {
        message.value = "$bytesSent bytes were sent."

        scheduleMessageDisappearance()
    }

    private fun scheduleMessageDisappearance() {
        hideMessageJob?.cancel()
        hideMessageJob = viewModelScope.launch {
            delay(MESSAGE_DISPLAY_DURATION)
            message.value = ""
        }

        messageProgressJob?.cancel()
        messageProgressJob = viewModelScope.launch {
            (0 until 10).forEach { progress ->
                messageProgress.value = 1.0f - progress / MESSAGE_DISPLAY_UPDATES.toFloat()
                delay(MESSAGE_DISPLAY_DURATION / MESSAGE_DISPLAY_UPDATES)
            }
        }
    }

    // message to be displayed to the user
    val message = mutableStateOf("")
    val messageProgress = mutableStateOf(0.0f)

    // if that is not null, we are currently editing a slot
    val currentSlotEditor = mutableStateOf<ZeEditor?>(null)

    // if that is not null, we are currently configuring which editor / template to use
    val currentTemplateChooser = mutableStateOf<ZeTemplateChooser?>(null)

    // which page should be displayed in the simulator?
    private val currentSimulatorSlot = mutableStateOf<ZeSlot>(ZeSlot.Name)
    private val openApiKey = OPENAI_API_KEY.ifBlank {
        preferencesService.getOpenApiKey()
    }

    val slots = mutableStateOf(
        mapOf(
            ZeSlot.Name to initialConfiguration(ZeSlot.Name),
            ZeSlot.FirstSponsor to initialConfiguration(ZeSlot.FirstSponsor),
            ZeSlot.SecondSponsor to initialConfiguration(ZeSlot.SecondSponsor),
            ZeSlot.FirstCustom to initialConfiguration(ZeSlot.FirstCustom),
            ZeSlot.SecondCustom to initialConfiguration(ZeSlot.SecondCustom),
            ZeSlot.BarCode to initialConfiguration(ZeSlot.BarCode),
            ZeSlot.QRCode to initialConfiguration(ZeSlot.QRCode),
            ZeSlot.Weather to initialConfiguration(ZeSlot.Weather),
            ZeSlot.Quote to initialConfiguration(ZeSlot.Quote),
        ),
    )

    /**
     * Call this method to send a given slot to the badge device.
     *
     * @param slot to be send.
     */
    fun sendPageToDevice(slot: ZeSlot) {
        message.value = ""

        if (!slots.value.contains(slot)) {
            Log.e("VM", "Slot $slot is not one of our slots.")
            return
        }

        val bitmap = slots.value[slot]!!.bitmap
        if (bitmap.isBinary()) {
            viewModelScope.launch {
                badgeUploader.sendPage(slot.name, bitmap).fold(
                    onSuccess = { badgeSuccess(it) },
                    onFailure = { badgeFailure(it.message ?: "Unknown error") },
                )
            }
        } else {
            showSnackBar("Please give binary image for page '${slot.name}'.")
        }
    }

    /**
     * Loop through given sponsor images.
     *
     * @param slot the slot to be configured.
     */
    fun customizeSponsorSlot(slot: ZeSlot) {
        when (slot) {
            is ZeSlot.FirstSponsor -> {
                slots.value = slots.value.copy(
                    slot to ZeConfiguration.Picture(
                        listOf(
                            R.drawable.page_google,
                            R.drawable.page_google_2,
                            R.drawable.page_google_3,
                        )
                            .random()
                            .toBitmap()
                            .ditherFloydSteinberg(),
                    ),
                )
            }

            is ZeSlot.SecondSponsor -> {
                slots.value = slots.value.copy(
                    slot to ZeConfiguration.Picture(
                        listOf(
                            R.drawable.page_telekom_2,
                            R.drawable.page_telekom_3,
                            R.drawable.page_telekom,
                        )
                            .random()
                            .toBitmap()
                            .ditherFloydSteinberg(),
                    ),
                )
            }

            else -> {}
        }
    }

    /**
     * Configure the given slot
     *
     * @param slot the slot to be configured.
     */
    fun customizeSlot(slot: ZeSlot) {
        message.value = ""

        // Do we need a template chooser first? Aka are we selecting a custom slot?
        if (slot in listOf(ZeSlot.FirstCustom, ZeSlot.SecondCustom)) {
            // yes, so let the user choose
            currentTemplateChooser.value = ZeTemplateChooser(
                slot = slot,
                configurations = mutableListOf(
                    ZeConfiguration.Name(
                        null,
                        null,
                        imageProviderService.getInitialNameBitmap(),
                    ), // TODO: Fetch from shared

                    ZeConfiguration.Picture(R.drawable.soon.toBitmap()),

                    ZeConfiguration.Schedule(
                        R.drawable.soon.toBitmap(),
                    ), // TODO: Fetch Schedule here.

                    ZeConfiguration.Weather(
                        "2023-07-06",
                        "26C",
                        R.drawable.soon.toBitmap(),
                    ),

                    ZeConfiguration.Kodee(
                        R.drawable.kodee.toBitmap().ditherFloydSteinberg()
                    ),
                    ZeConfiguration.ImageDraw(
                        R.drawable.kodee.toBitmap().ditherFloydSteinberg(),
                    ),
                    ZeConfiguration.Camera(R.drawable.soon.toBitmap().ditherFloydSteinberg()),
                    ZeConfiguration.Camera(R.drawable.soon.toBitmap().ditherFloydSteinberg()),
                    ZeConfiguration.CustomPhrase(
                        "Custom phrase",
                        R.drawable.page_phrase.toBitmap().ditherFloydSteinberg()
                    )
                ).apply {
                    // Surprise mechanic: If token is set, show open ai item
                    if (openApiKey.isNotBlank()) {
                        add(
                            2,
                            ZeConfiguration
                                .ImageGen(
                                    prompt = "An Android developer at a conference in Berlin.",
                                    bitmap = R.drawable.soon.toBitmap(),
                                ),
                        )
                    }
                },
            )
        } else {
            // no selection needed, check for name slot and ignore non configurable slots
            if (slot is ZeSlot.Name) {
                currentSlotEditor.value = ZeEditor(
                    slot,
                    slots.value[ZeSlot.Name]!!
                )
            } else if (slot is ZeSlot.QRCode) {
                currentSlotEditor.value = ZeEditor(
                    slot,
                    slots.value[ZeSlot.QRCode]!!
                )
            } else if (slot is ZeSlot.Weather) {
                currentSlotEditor.value = ZeEditor(
                    slot,
                    slots.value[ZeSlot.Weather]!!
                )
            } else if(slot is ZeSlot.BarCode) {
                currentSlotEditor.value = ZeEditor(
                    slot,
                    slots.value[ZeSlot.BarCode]!!
                )
            } else if (slot is ZeSlot.Quote) {
                currentSlotEditor.value = ZeEditor(
                    slot,
                    slots.value[ZeSlot.Quote]!!
                )
            } else {
                Log.d("Customize Page", "Cannot configure slot '${slot.name}'.")
            }
        }
    }

    /**
     * User just selected the template to apply to a given slot, so open the according editor
     *
     * @param slot the slot to be changed, null if discarded
     * @param configuration the configuration of the slot, null if discarded
     */
    fun templateSelected(slot: ZeSlot?, configuration: ZeConfiguration?) {
        currentTemplateChooser.value = null

        if (slot != null && configuration != null) {
            currentSlotEditor.value = ZeEditor(slot, configuration)
        }
    }

    /**
     * Editor closing, so the slot is configured successfully, unless parameters are null
     *
     * @param slot the slot configured.
     * @param configuration the configuration of the slot.
     */
    fun slotConfigured(slot: ZeSlot?, configuration: ZeConfiguration?) {
        currentSlotEditor.value = null

        if (slot != null && configuration != null) {
            slots.value = slots.value.copy(slot to configuration)
            slot.save()
        }
    }

    /**
     * In <em>Simulator Mode</em> this method will trigger display of the given slot
     *
     * @param slot the slot to be displayed.
     */
    fun simulatorButtonPressed(slot: ZeSlot) {
        currentSimulatorSlot.value = slot
    }

    /**
     * Convert the given slot to a bitmap
     *
     * This could be used to display the slot in the UI, or to send it to the device internally.
     *
     * @param slot the slot to be converted
     */
    fun slotToBitmap(slot: ZeSlot = currentSimulatorSlot.value): Bitmap =
        slots.value[slot]?.bitmap ?: R.drawable.error.toBitmap().also {
            Log.d("Slot to Bitmap", "Unavailable slot tried to fetch bitmap.")
        }

    /**
     * Reset the given slot to it's defaults when starting the app
     *
     * @param slot the slot to be defaulted
     */
    fun resetSlot(slot: ZeSlot) {
        message.value = ""

        slots.value = slots.value.copy(slot to initialConfiguration(slot))
    }

    private fun initialConfiguration(slot: ZeSlot): ZeConfiguration {
        if (preferencesService.isSlotConfigured(slot)) {
            val configuration = preferencesService.getSlotConfiguration(slot)
            if (configuration != null) {
                return configuration
            }
        }

        return when (slot) {
            is ZeSlot.Name -> ZeConfiguration.Name(
                null,
                null,
                imageProviderService.getInitialNameBitmap()
            )

            is ZeSlot.FirstSponsor -> ZeConfiguration.Picture(R.drawable.page_google.toBitmap())
            is ZeSlot.SecondSponsor -> ZeConfiguration.Picture(R.drawable.page_telekom.toBitmap())
            is ZeSlot.FirstCustom -> ZeConfiguration.Picture(R.drawable.soon.toBitmap())
            is ZeSlot.SecondCustom -> ZeConfiguration.Picture(R.drawable.soon.toBitmap())
            ZeSlot.QRCode -> ZeConfiguration.QRCode(
                "Your title",
                "",
                "",
                R.drawable.qrpage_preview.toBitmap(),
            )
            ZeSlot.Weather -> ZeConfiguration.Weather(
                "2023-07-06",
                "22C",
                R.drawable.soon.toBitmap(),
            )

            is ZeSlot.Quote -> ZeConfiguration.Quote(
                "Test",
                "Author",
                R.drawable.page_quote_sample.toBitmap(),
            )

            ZeSlot.BarCode -> ZeConfiguration.BarCode(
                "Your title for barcode",
                "",
                R.drawable.soon.toBitmap(),
            )
        }
    }

    /**
     * Save all slots to shared preferences.
     */
    fun saveAll() {
        for (slot in slots.value.keys) {
            slot.save()
        }
    }

    /**
     * Sends a random page to the badge
     */
    fun sendRandomPageToDevice() {
        sendPageToDevice(slots.value.keys.random())
    }

    private fun Int.toBitmap(): Bitmap {
        return imageProviderService.provideImageBitmap(this)
    }

    private fun ZeSlot.save() {
        val config = slots.value[this]!!
        preferencesService.saveSlotConfiguration(this, config)
    }

    fun copyInfoToClipboard() {
        clipboardService.copyToClipboard(message.value)
        showSnackBar("Copied")
    }

    val lines: StateFlow<List<String>> = contributorsService.contributors()
        .stateIn(viewModelScope, SharingStarted.Lazily, initialValue = emptyList())
}

private fun <K, V> Map<K, V>.copy(vararg entries: Pair<K, V>): Map<K, V> {
    val result = toMutableMap()

    entries.forEach { entry ->
        val (replaceKey: K, replaceValue: V) = entry
        result[replaceKey] = replaceValue
    }

    return result.toMap()
}
