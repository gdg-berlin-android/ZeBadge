package de.berlindroid.zeapp.zevm

import android.app.Application
import android.content.ClipData
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.berlindroid.zeapp.OPENAI_API_KEY
import de.berlindroid.zeapp.PAGE_HEIGHT
import de.berlindroid.zeapp.PAGE_WIDTH
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.zebits.ditherFloydSteinberg
import de.berlindroid.zeapp.zebits.isBinary
import de.berlindroid.zeapp.zebits.scaleIfNeeded
import de.berlindroid.zeapp.zebits.toBinary
import de.berlindroid.zeapp.zebits.toBitmap
import de.berlindroid.zeapp.zehardware.Badge
import de.berlindroid.zeapp.zehardware.base64
import de.berlindroid.zeapp.zehardware.debase64
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val OPEN_API_PREFERENCES_KEY = "openapi"

private const val MESSAGE_DISPLAY_DURATION = 3_000L
private const val MESSAGE_DISPLAY_UPDATES = 5

/**
 * Base ViewModel building a list of pages for the badge and offering simulator support.
 */
class ZeBadgeViewModel(
    application: Application,
) : AndroidViewModel(application) {
    /**
     * Slot a page can reside in.
     *
     */
    sealed class Slot(val name: String) {
        object Name : Slot("A")
        object FirstSponsor : Slot("B")
        object SecondSponsor : Slot("C")
        object FirstCustom : Slot("Up")
        object SecondCustom : Slot("Down")
        object QRCode : Slot("Q")
    }

    /**
     * The configuration of a slot
     *
     * Add your own configuration here if you want a new page.
     *
     * Every inheritor should contain a companion object TYPE field, so it's type can be retrieved
     * from saved places like shared preferences or the hardware badge.
     *
     * @param humanTitle is the title to be used to interact with so called humans
     * @param bitmap the bitmap created, might be empty or an error bitmap at first
     */
    sealed class Configuration(
        open val humanTitle: String,
        open val bitmap: Bitmap,
    ) {
        /**
         * Store the name and contact of an attendee.
         *
         * @param name the name of the attendee ("Jane Doe")
         * @param contact used for contacting the attendee ("jane@doe.com")
         * @param bitmap (overriden) final page
         */
        data class Name(
            val name: String,
            val contact: String,
            override val bitmap: Bitmap,
        ) : Configuration(TYPE, bitmap) {
            companion object {
                const val TYPE: String = "Name Tag"
            }
        }

        /**
         * Store the name and contact of an attendee.
         *
         * @param url the URL of the attendee github profile ("https://github.com/gdg-berlin-android")
         * @param bitmap (overriden) final page
         */
        data class QRCode(
            val title: String,
            val url: String,
            override val bitmap: Bitmap,
        ) : Configuration(TYPE, bitmap) {
            companion object {
                const val TYPE: String = "QRCode Tag"
            }
        }

        /**
         * A picture to be displayed as the page.
         *
         * Only the actual bitmap is needed, since it is not assumed, that the picture can be
         * retrieved later on again.
         *
         * @param bitmap the page bitmap to be shown
         */
        data class Picture(
            override val bitmap: Bitmap,
        ) : Configuration(TYPE, bitmap) {
            companion object {
                const val TYPE: String = "Custom Picture"
            }
        }

        /**
         * Configure this slot for image generation
         *
         * Favorite configuration of Mario so far, try and convince him otherwise: This
         * configuration will be used to contact Dalle2 and generate the an image for the prompt.
         *
         * @param prompt describe the contents of the page to be created.
         * @param bitmap the resulting page.
         */
        data class ImageGen(
            val prompt: String,
            override val bitmap: Bitmap,
        ) : Configuration(TYPE, bitmap) {
            companion object {
                const val TYPE: String = "Image Gen"
            }
        }

        // ADD CUSTOM PAGES HERE

        /**
         * TODO: This configuration is a place holder to entice you, the reader to build it
         *
         * Think of it as a teaser: How would you configure a page that contains the schedule of
         * the droidcon 2023 in Berlin?
         *
         * @param bitmap the schedule to be displayed as a page.
         */
        data class Schedule(
            override val bitmap: Bitmap,
        ) : Configuration(TYPE, bitmap) {
            companion object {
                const val TYPE: String = "Conference Schedule"
            }
        }

        /**
         * TODO: Teaser nr 2: Build a configuration and editor to create a page showing the weather
         *
         * How would you go for getting the weather? What do you need to configure here, so that an
         * editor can fill it in?
         */
        data class Weather(
            override val bitmap: Bitmap,
        ) : Configuration(TYPE, bitmap) {
            companion object {
                const val TYPE: String = "Upcoming Weather"
            }
        }

        // TODO: Add your own pages.
    }

    /**
     * State of the current editor
     *
     * Use to display an editor to update the current slot's configuration.
     *
     * @param slot the slot of the badge the current editor works on
     * @param config the initial configuration of slot to be worked on by the editor.
     */
    data class Editor(
        val slot: Slot,
        val config: Configuration
    )

    /**
     * State of which configurations can be applied to the selected slot.
     *
     * Used for creating a chooser in the ui, to select which editor should be used next.
     *
     * @param slot stores which slot the to be selected configuration should be applied to.
     * @param configurations a list of valid configuration of this slot.
     */
    data class TemplateChooser(
        val slot: Slot,
        val configurations: List<Configuration>,
    )

    // save all the things!
    private val sharedPreferences =
        getApplication<Application>()
            .getSharedPreferences(
                "defaults",
                Application.MODE_PRIVATE
            )

    // Hardware communication interface
    private val badge = Badge(
        ::badgeSuccess,
        ::badgeFailure,
    )

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
    val currentSlotEditor = mutableStateOf<Editor?>(null)

    // if that is not null, we are currently configuring which editor / template to use
    val currentTemplateChooser = mutableStateOf<TemplateChooser?>(null)

    // which page should be displayed in the simulator?
    val currentSimulatorSlot = mutableStateOf<Slot>(Slot.Name)
    val openApiKey = mutableStateOf(
        OPENAI_API_KEY.ifBlank {
            sharedPreferences.getString(OPEN_API_PREFERENCES_KEY, "")
        }
    )

    val slots = mutableStateOf(
        mapOf(
            Slot.Name to initialConfiguration(Slot.Name),
            Slot.Name to initialConfiguration(Slot.Name),
            Slot.FirstSponsor to initialConfiguration(Slot.FirstSponsor),
            Slot.SecondSponsor to initialConfiguration(Slot.SecondSponsor),
            Slot.FirstCustom to initialConfiguration(Slot.FirstCustom),
            Slot.SecondCustom to initialConfiguration(Slot.SecondCustom),
            Slot.QRCode to initialConfiguration(Slot.QRCode),
        )
    )

    /**
     * Call this method to send a given slot to the badge device.
     *
     * @param slot to be send.
     */
    fun sendPageToDevice(slot: Slot) {
        message.value = ""

        if (!slots.value.contains(slot)) {
            Log.e("VM", "Slot $slot is not one of our slots.")
            return
        }

        val bitmap = slots.value[slot]!!.bitmap
        if (bitmap.isBinary()) {
            badge.sendPage(
                getApplication<Application>().applicationContext,
                slot.name,
                bitmap
            )
        } else {
            Toast.makeText(
                getApplication(),
                "Please give binary image for page '${slot.name}'.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Loop through given sponsor images.
     *
     * @param slot the slot to be configured.
     */
    fun customizeSponsorSlot(slot: Slot) {
        when (slot) {
            is Slot.FirstSponsor -> {
                slots.value = slots.value.copy(
                    slot to Configuration.Picture(
                        listOf(
                            R.drawable.page_google,
                            R.drawable.page_google_2,
                            R.drawable.page_google_3,
                        )
                            .random()
                            .toBitmap()
                            .ditherFloydSteinberg()
                    )
                )
            }

            is Slot.SecondSponsor -> {
                slots.value = slots.value.copy(
                    slot to Configuration.Picture(
                        listOf(
                            R.drawable.page_telekom_2,
                            R.drawable.page_telekom_3,
                            R.drawable.page_telekom,
                        )
                            .random()
                            .toBitmap()
                            .ditherFloydSteinberg()
                    )
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
    fun customizeSlot(slot: Slot) {
        message.value = ""

        // Do we need a template chooser first? Aka are we selecting a custom slot?
        if (slot in listOf(Slot.FirstCustom, Slot.SecondCustom)) {
            // yes, so let the user choose
            currentTemplateChooser.value = TemplateChooser(
                slot = slot,
                configurations = mutableListOf(
                    Configuration.Name(
                        "Your Name",
                        "Your Contact",
                        initialNameBitmap()
                    ), // TODO: Fetch from shared

                    Configuration.Picture(R.drawable.soon.toBitmap()),

                    Configuration.Schedule(
                        R.drawable.soon.toBitmap()
                    ), // TODO: Fetch Schedule here.

                    Configuration.Weather(
                        R.drawable.soon.toBitmap()
                    ), // TODO: Fetch weather here
                ).apply {
                    // Surprise mechanic: If token is set, show open ai item
                    if (openApiKey.value.isNeitherNullNorBlank()) {
                        add(
                            2,
                            Configuration
                                .ImageGen(
                                    prompt = "An Android developer at a conference in Berlin.",
                                    bitmap = R.drawable.soon.toBitmap()
                                )
                        )
                    }
                }
            )
        } else {
            // no selection needed, check for name slot and ignore non configurable slots
            if (slot is Slot.Name) {
                currentSlotEditor.value = Editor(
                    slot,
                    slots.value[Slot.Name]!!
                )
            } else if (slot is Slot.QRCode) {
                currentSlotEditor.value = Editor(
                    slot,
                    slots.value[Slot.QRCode]!!
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
    fun templateSelected(slot: Slot?, configuration: Configuration?) {
        currentTemplateChooser.value = null

        if (slot != null && configuration != null) {
            currentSlotEditor.value = Editor(slot, configuration)
        }
    }

    /**
     * Editor closing, so the slot is configured successfully, unless parameters are null
     *
     * @param slot the slot configured.
     * @param configuration the configuration of the slot.
     */
    fun slotConfigured(slot: Slot?, configuration: Configuration?) {
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
    fun simulatorButtonPressed(slot: Slot) {
        currentSimulatorSlot.value = slot
    }

    /**
     * Convert the given slot to a bitmap
     *
     * This could be used to display the slot in the UI, or to send it to the device internally.
     *
     * @param slot the slot to be converted
     */
    fun slotToBitmap(slot: Slot = currentSimulatorSlot.value): Bitmap =
        slots.value[slot]?.bitmap ?: R.drawable.error.toBitmap().also {
            Log.d("Slot to Bitmap", "Unavailable slot tried to fetch bitmap.")
        }

    /**
     * Reset the given slot to it's defaults when starting the app
     *
     * @param slot the slot to be defaulted
     */
    fun resetSlot(slot: Slot) {
        message.value = ""

        slots.value = slots.value.copy(slot to initialConfiguration(slot))
    }

    private fun initialConfiguration(slot: Slot): Configuration {
        if (slot.isStoredInPreferences()) {
            val configuration = slot.fromPreferences()
            if (configuration != null) {
                return configuration
            }
        }

        return when (slot) {
            is Slot.Name -> Configuration.Name(
                "Your Name",
                "Your Contact",
                initialNameBitmap()
            )

            is Slot.FirstSponsor -> Configuration.Picture(R.drawable.page_google.toBitmap())
            is Slot.SecondSponsor -> Configuration.Picture(R.drawable.page_telekom.toBitmap())
            is Slot.FirstCustom -> Configuration.Picture(R.drawable.soon.toBitmap())
            is Slot.SecondCustom -> Configuration.Picture(R.drawable.soon.toBitmap())
            Slot.QRCode -> Configuration.QRCode(
                "Your title",
                "",
                R.drawable.soon.toBitmap()
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

    private fun initialNameBitmap(): Bitmap =
        BitmapFactory.decodeResource(
            getApplication<Application>().resources,
            R.drawable.sample_badge,
        ).scaleIfNeeded(PAGE_WIDTH, PAGE_HEIGHT)

    private fun Int.toBitmap(): Bitmap =
        BitmapFactory.decodeResource(
            getApplication<Application>().resources,
            this,
            BitmapFactory.Options().apply { inScaled = false }
        ).scaleIfNeeded(PAGE_WIDTH, PAGE_HEIGHT)

    private fun Slot.save() {
        val config = slots.value[this]!!

        sharedPreferences.edit()
            .putConfig(this, config)
            .apply()
    }

    private fun SharedPreferences.Editor.putConfig(
        slot: Slot,
        config: Configuration
    ): SharedPreferences.Editor {
        putString(slot.preferencesTypeKey(), config.humanTitle)
        putString(slot.preferencesBitmapKey(), config.bitmap.toBinary().base64())

        when (config) {
            is Configuration.Name -> {
                putString(slot.preferencesKey("name"), config.name)
                putString(slot.preferencesKey("contact"), config.contact)
            }

            is Configuration.ImageGen -> {
                putString(slot.preferencesKey("prompt"), config.prompt)
            }

            is Configuration.Picture -> {
                // Nothing more to configure
            }

            is Configuration.Schedule -> {
                // TODO: Save schedule
            }

            is Configuration.Weather -> {
                // TODO: Save weather
            }

            is Configuration.QRCode -> {
                putString(slot.preferencesKey("qr_title"), config.title)
                putString(slot.preferencesKey("url"), config.url)
            }
        }

        return this
    }

    private fun Slot.isStoredInPreferences(): Boolean =
        sharedPreferences.contains(preferencesTypeKey())

    private fun Slot.fromPreferences(): Configuration? {
        val type = preferencesType()
        val bitmap = preferencesBitmap()

        return when (type) {
            Configuration.Name.TYPE -> {
                Configuration.Name(
                    name = preferencesValue("name"),
                    contact = preferencesValue("contact"),
                    bitmap = bitmap,
                )
            }

            Configuration.Picture.TYPE -> Configuration.Picture(bitmap)

            Configuration.ImageGen.TYPE -> Configuration.ImageGen(
                prompt = preferencesValue("prompt"),
                bitmap = bitmap
            )

            Configuration.Schedule.TYPE -> Configuration.Schedule(bitmap)

            Configuration.Weather.TYPE -> Configuration.Weather(bitmap)

            Configuration.QRCode.TYPE -> Configuration.QRCode(
                title = preferencesValue("qr_title"),
                url = preferencesValue("url"),
                bitmap = bitmap
            )

            else -> {
                Log.e(
                    "Slot from Prefs",
                    "Cannot find $type slot in preferences."
                )
                null
            }
        }
    }

    private fun Slot.preferencesTypeKey(): String = preferencesKey("type")

    private fun Slot.preferencesType(): String = preferencesValue("type")

    private fun Slot.preferencesBitmapKey(): String = preferencesKey("bitmap")

    private fun Slot.preferencesBitmap(): Bitmap = preferencesValue("bitmap")
        .debase64()
        .toBitmap()

    private fun Slot.preferencesKey(field: String): String =
        "slot.$name.$field"

    private fun Slot.preferencesValue(field: String): String =
        sharedPreferences.getString(preferencesKey(field), "")
            .orEmpty()

    fun copyInfoToClipboard() {
        message.value?.let {
            val context = getApplication<Application>().applicationContext
            val manager = context.getSystemService(android.content.ClipboardManager::class.java)

            manager.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.app_name), it))
            Toast.makeText(context, "Copied", Toast.LENGTH_LONG).show()
        }
    }
}

private fun <K, V> Map<K, V>.copy(vararg entries: Pair<K, V>): Map<K, V> {
    val result = mutableMapOf<K, V>()

    entries.forEach { entry ->
        val (replaceKey: K, replaceValue: V) = entry

        forEach { (key, value) ->
            result[key] = if (replaceKey == key) {
                replaceValue
            } else {
                value
            }
        }
    }

    return result.toMap()
}

private fun String?.isNeitherNullNorBlank(): Boolean = !this.isNullOrBlank()
