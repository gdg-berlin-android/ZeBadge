package de.berlindroid.zeapp.zevm

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.berlindroid.zeapp.OPENAI_API_KEY
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.zebits.isBinary
import de.berlindroid.zeapp.zemodels.ZeConfiguration
import de.berlindroid.zeapp.zemodels.ZeEditor
import de.berlindroid.zeapp.zemodels.ZeSlot
import de.berlindroid.zeapp.zemodels.ZeTemplateChooser
import de.berlindroid.zeapp.zeservices.*
import de.berlindroid.zeapp.zeui.ZeCameraEditor
import de.berlindroid.zeapp.zeui.pixelManipulation
import de.berlindroid.zekompanion.ditherFloydSteinberg
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

private const val MESSAGE_DISPLAY_DURATION = 3_000L
private const val MESSAGE_DISPLAY_UPDATES = 5

/**
 * Base ViewModel building a list of pages for the badge and offering simulator support.
 */
@HiltViewModel
class ZeBadgeViewModel @Inject constructor(
    private val imageProviderService: ZeImageProviderService,
    private val badgeManager: ZeBadgeManager,
    private val preferencesService: ZePreferencesService,
    private val clipboardService: ZeClipboardService,
    private val getTemplateConfigurations: GetTemplateConfigurations,
    contributorsService: ZeContributorsService,
) : ViewModel() {

    private val _uiState: MutableStateFlow<ZeBadgeUiState> = MutableStateFlow(getInitialUIState())
    val uiState: StateFlow<ZeBadgeUiState> = _uiState.asStateFlow()

    // See if disappearing message is ongoing
    private var hideMessageJob: Job? = null
    private var messageProgressJob: Job? = null

    fun showMessage(
        message: String,
        duration: Long = MESSAGE_DISPLAY_DURATION,
    ) {
        _uiState.update {
            it.copy(message = it.message + message)
        }

        scheduleMessageDisappearance(duration)
    }

    private fun scheduleMessageDisappearance(
        duration: Long = MESSAGE_DISPLAY_DURATION,
    ) {
        hideMessageJob?.cancel()
        hideMessageJob = viewModelScope.launch {
            delay(duration)
            _uiState.update {
                it.copy(message = "")
            }
        }

        messageProgressJob?.cancel()
        messageProgressJob = viewModelScope.launch {
            for (progress in 0 until MESSAGE_DISPLAY_UPDATES) {
                _uiState.update {
                    it.copy(messageProgress = 1.0f - progress / MESSAGE_DISPLAY_UPDATES.toFloat())
                }
                delay(duration / MESSAGE_DISPLAY_UPDATES)
            }
        }
    }

    private val openApiKey = OPENAI_API_KEY.ifBlank {
        runBlocking(viewModelScope.coroutineContext) { preferencesService.getOpenApiKey() }
    }

    /**
     * Call this method to send a given slot to the badge device.
     *
     * @param slot to be sent.
     */
    fun sendPageToBadgeAndDisplay(slot: ZeSlot) {
        _uiState.update {
            it.copy(message = "")
        }

        val slots = _uiState.value.slots

        val configuration = slots.getOrElse(slot) {
            Timber.e("VM", "Slot $slot is not one of our slots.")
            null
        } ?: return

        if (!badgeManager.isConnected()) {
            showMessage("Please connect a badge.")
            return
        }

        val bitmap = configuration.bitmap
        if (bitmap.isBinary()) {
            viewModelScope.launch {
                badgeManager.storePage(configuration.type.name, bitmap).fold(
                    onSuccess = { storeResult ->
                        delay(300) // serial stuff
                        badgeManager.showPage(configuration.type.name).fold(
                            onSuccess = { showResult ->
                                showMessage(
                                    // Hadouken¹
                                    "${showResult + storeResult} bytes were sent.",
                                )
                            },
                            onFailure = { showMessage("❗${it.message ?: "Unknown error"} ❗") },
                        )
                    },
                    onFailure = { showMessage("❗${it.message ?: "Unknown error"} ❗") },
                )
            }
        } else {
            showMessage("Please create a binary image for page '${slot.name}'.")
        }
    }

    /**
     * Loop through given sponsor images.
     *
     * @param slot the slot to be configured.
     */
    fun customizeSponsorSlot(slot: ZeSlot) {
        val slots = _uiState.value.slots
        when (slot) {
            is ZeSlot.FirstSponsor -> {
                val slotsCopy = slots.copy(
                    slot to ZeConfiguration.Picture(
                        listOf(
                            R.drawable.page_google,
                            R.drawable.page_google_2,
                            R.drawable.page_google_3,
                        )
                            .random()
                            .toBitmap()
                            .pixelManipulation { w, h -> ditherFloydSteinberg(w, h) },
                    ),
                )
                _uiState.update {
                    it.copy(slots = slotsCopy)
                }
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
        _uiState.update {
            it.copy(message = "")
        }

        // Do we need a template chooser first? Aka are we selecting a custom slot?
        if (slot in listOf(ZeSlot.FirstCustom, ZeSlot.SecondCustom)) {
            // yes, so let the user choose
            val newCurrentTemplateChooser = ZeTemplateChooser(
                slot = slot,
                configurations = getTemplateConfigurations(openApiKey),
            )
            _uiState.update {
                it.copy(currentTemplateChooser = newCurrentTemplateChooser)
            }
        } else {
            // no selection needed, check for name slot and ignore non configurable slots
            val slots = _uiState.value.slots
            val newCurrentSlotEditor = when (slot) {
                is ZeSlot.Name -> ZeEditor(
                    slot,
                    slots[ZeSlot.Name]!!,
                )

                is ZeSlot.QRCode -> ZeEditor(
                    slot,
                    slots[ZeSlot.QRCode]!!,
                )

                is ZeSlot.Weather -> ZeEditor(
                    slot,
                    slots[ZeSlot.Weather]!!,
                )

                is ZeSlot.BarCode -> ZeEditor(
                    slot,
                    slots[ZeSlot.BarCode]!!,
                )

                is ZeSlot.Add -> ZeEditor(
                    slot,
                    slots[ZeSlot.Add]!!,
                )

                is ZeSlot.Camera -> ZeEditor(
                    slot,
                    slots[ZeSlot.Camera]!!,
                )

                else -> {
                    Timber.d("Customize Page", "Cannot configure slot '${slot.name}'.")
                    null
                }
            }
            newCurrentSlotEditor?.let { currentSlotEditor ->
                _uiState.update {
                    it.copy(currentSlotEditor = currentSlotEditor)
                }
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
        var currentSlotEditor: ZeEditor? = null
        if (slot != null && configuration != null) {
            currentSlotEditor = ZeEditor(slot, configuration)
        }

        _uiState.update {
            if (currentSlotEditor != null) {
                it.copy(
                    currentTemplateChooser = null,
                    currentSlotEditor = currentSlotEditor,
                )
            } else {
                it.copy(currentTemplateChooser = null)
            }
        }
    }

    /**
     * Editor closing, so the slot is configured successfully, unless parameters are null
     *
     * @param slot the slot configured.
     * @param configuration the configuration of the slot.
     */
    fun slotConfigured(slot: ZeSlot?, configuration: ZeConfiguration?) {
        var newSlots: Map<ZeSlot, ZeConfiguration>? = null
        if (slot != null && configuration != null) {
            val slots = _uiState.value.slots
            newSlots = slots.copy(slot to configuration)
            saveSlotConfiguration(slot, configuration)
        }
        _uiState.update {
            if (newSlots != null) {
                it.copy(currentSlotEditor = null, slots = newSlots)
            } else {
                it.copy(currentSlotEditor = null)
            }
        }
    }

    /**
     * In <em>Simulator Mode</em> this method will trigger display of the given slot
     *
     * @param slot the slot to be displayed.
     */
    fun simulatorButtonPressed(slot: ZeSlot) {
        _uiState.update {
            it.copy(currentSimulatorSlot = slot)
        }
    }

    /**
     * Convert the given slot to a bitmap
     *
     * This could be used to display the slot in the UI, or to send it to the device internally.
     *
     * @param slot the slot to be converted
     */
    fun slotToBitmap(slot: ZeSlot = _uiState.value.currentSimulatorSlot): Bitmap {
        val slots = _uiState.value.slots
        return slots[slot]?.bitmap ?: R.drawable.error.toBitmap().also {
            Timber.d("Slot to Bitmap", "Unavailable slot tried to fetch bitmap.")
        }
    }

    /**
     * Reset the given slot to it's defaults when starting the app
     *
     * @param slot the slot to be defaulted
     */
    fun resetSlot(slot: ZeSlot) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    message = "",
                    slots = it.slots.copy(slot to initialConfiguration(slot)),
                )
            }
        }
    }

    private suspend fun initialConfiguration(slot: ZeSlot): ZeConfiguration {
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
                imageProviderService.getInitialNameBitmap(),
            )

            is ZeSlot.FirstSponsor -> ZeConfiguration.Picture(R.drawable.page_google.toBitmap())
            is ZeSlot.FirstCustom -> ZeConfiguration.Picture(R.drawable.soon.toBitmap())
            is ZeSlot.SecondCustom -> ZeConfiguration.Picture(R.drawable.soon.toBitmap())
            ZeSlot.QRCode -> ZeConfiguration.QRCode(
                title = "",
                text = "",
                url = "",
                isVcard = false,
                phone = "",
                email = "",
                bitmap = R.drawable.qrpage_preview.toBitmap(),
            )

            ZeSlot.Weather -> ZeConfiguration.Weather(
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
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

            ZeSlot.Add -> ZeConfiguration.Name(
                null,
                null,
                imageProviderService.provideImageBitmap(R.drawable.add),
            )

            ZeSlot.Camera -> ZeConfiguration.Camera(
                imageProviderService.provideImageBitmap(R.drawable.soon),
            )
        }
    }

    /**
     * Save all slots to shared preferences and the badge.
     */
    fun saveAll() {
        val slots = _uiState.value.slots
        for ((slot, configuration) in slots) {
            saveSlotConfiguration(slot, configuration)
        }
        storePages(slots)
    }

    private fun storePages(slots: Map<ZeSlot, ZeConfiguration>) {
        if (!badgeManager.isConnected()) {
            showMessage("Please connect a badge.")
        } else {
            viewModelScope.launch {
                for ((slot, config) in slots) {
                    val stored = badgeManager.storePage(slot.name, config.bitmap)
                    if (stored.isFailure) {
                        showMessage("Could not send page.")
                    } else {
                        showMessage("Page in slot '${slot.name}' send successfully.\n")
                    }
                }
            }
        }
    }

    /**
     * Talks to the badge to get all stored pages from the badge
     */
    fun getStoredPages() {
        if (!badgeManager.isConnected()) {
            showMessage("Please connect a badge.")
        } else {
            viewModelScope.launch {
                val stored = badgeManager.requestPagesStored()
                if (stored.isSuccess) {
                    val message = stored.getOrNull()
                    if (message != null) {
                        showMessage(message.replace(",", "\n"))
                    }
                }
            }
        }
    }

    /**
     * Talks to the badge to get the current active configuration
     */
    fun listConfiguration() {
        if (!badgeManager.isConnected()) {
            showMessage("Please connect a badge.")
        } else {
            viewModelScope.launch {
                val configResult = badgeManager.listConfiguration()
                if (configResult.isSuccess) {
                    val kv = configResult.getOrNull()
                    if (kv != null) {
                        showMessage(kv.toString())
                        _uiState.update {
                            it.copy(currentBadgeConfig = kv)
                        }
                    }
                }
            }
        }
    }

    fun updateConfiguration(configuration: Map<String, Any?>) {
        _uiState.update {
            it.copy(currentBadgeConfig = null)
        }

        if (!badgeManager.isConnected()) {
            showMessage("Please connect a badge.")
        } else {
            viewModelScope.launch {
                badgeManager.updateConfiguration(configuration)
                _uiState.update {
                    it.copy(currentBadgeConfig = null)
                }
            }
        }
    }

    fun closeConfiguration() {
        _uiState.update {
            it.copy(currentBadgeConfig = null)
        }
    }

    private fun Int.toBitmap(): Bitmap {
        return imageProviderService.provideImageBitmap(this)
    }

    private fun saveSlotConfiguration(slot: ZeSlot, config: ZeConfiguration) {
        viewModelScope.launch(Dispatchers.IO) {
            preferencesService.saveSlotConfiguration(slot, config)
        }
    }

    fun copyInfoToClipboard() {
        clipboardService.copyToClipboard(_uiState.value.message)
        showMessage("Copied")
    }

    /**
     * Loads data from Datastore
     */
    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            val slots = mapOf(
                ZeSlot.Name to initialConfiguration(ZeSlot.Name),
                ZeSlot.FirstSponsor to initialConfiguration(ZeSlot.FirstSponsor),
                ZeSlot.Camera to initialConfiguration(ZeSlot.Camera),
                ZeSlot.Add to initialConfiguration(ZeSlot.Add),
            )
            _uiState.update {
                it.copy(slots = slots)
            }
        }
    }

    val lines: StateFlow<List<String>> = contributorsService.contributors()
        .stateIn(viewModelScope, SharingStarted.Lazily, initialValue = emptyList())

    private fun getInitialUIState(): ZeBadgeUiState =
        ZeBadgeUiState(
            message = "",
            messageProgress = 0.0f,
            currentSlotEditor = null,
            currentTemplateChooser = null,
            currentSimulatorSlot = ZeSlot.Name,
            slots = emptyMap(),
            currentBadgeConfig = null,
        )
}

data class ZeBadgeUiState(
    val message: String, // message to be displayed to the user
    val messageProgress: Float,
    val currentSlotEditor: ZeEditor?, // if that is not null, we are currently editing a slot
    val currentTemplateChooser: ZeTemplateChooser?, // if that is not null, we are currently configuring which editor / template to use
    val currentSimulatorSlot: ZeSlot, // which page should be displayed in the simulator?
    val slots: Map<ZeSlot, ZeConfiguration>,
    val currentBadgeConfig: Map<String, Any?>?,
)

// ¹ https://www.reddit.com/r/ProgrammerHumor/comments/27yykv/indent_hadouken/
