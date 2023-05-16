package de.berlindroid.zeapp.vm

import android.app.Application
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import de.berlindroid.zeapp.OPENAI_API_KEY
import de.berlindroid.zeapp.PAGE_HEIGHT
import de.berlindroid.zeapp.PAGE_WIDTH
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.bits.isBinary
import de.berlindroid.zeapp.bits.scaleIfNeeded
import de.berlindroid.zeapp.bits.toBinary
import de.berlindroid.zeapp.bits.toBitmap
import de.berlindroid.zeapp.hardware.Badge
import de.berlindroid.zeapp.hardware.base64
import de.berlindroid.zeapp.hardware.debase64

private const val OPEN_API_PREFERENCES_KEY = "openapi"

class BadgeViewModel(
    application: Application,
) : AndroidViewModel(application) {
    sealed class Slot(val name: String) {
        object Name : Slot("A")
        object FirstSponsor : Slot("B")
        object SecondSponsor : Slot("C")
        object FirstCustom : Slot("Up")
        object SecondCustom : Slot("Down")
    }

    sealed class Configuration(
        open val humanTitle: String,
        open val bitmap: Bitmap,
    ) {
        data class Name(
            val name: String,
            val contact: String,
            override val bitmap: Bitmap,
        ) : Configuration(TYPE, bitmap) {
            companion object {
                const val TYPE: String = "Name Tag"
            }
        }

        data class Picture(
            override val bitmap: Bitmap,
        ) : Configuration(TYPE, bitmap) {
            companion object {
                const val TYPE: String = "Custom Picture"
            }
        }

        data class ImageGen(
            val prompt: String,
            override val bitmap: Bitmap,
        ) : Configuration(TYPE, bitmap) {
            companion object {
                const val TYPE: String = "Image Gen"
            }
        }

        data class Schedule(
            override val bitmap: Bitmap,
        ) : Configuration(TYPE, bitmap) {
            companion object {
                const val TYPE: String = "Conference Schedule"
            }
        }

        data class Weather(
            override val bitmap: Bitmap,
        ) : Configuration(TYPE, bitmap) {
            companion object {
                const val TYPE: String = "Upcoming Weather"
            }
        }
    }

    data class Editor(
        val slot: Slot,
        val config: Configuration
    )

    data class TemplateChooser(
        val slot: Slot,
        val configurations: List<Configuration>,
    )

    private val sharedPreferences =
        getApplication<Application>()
            .getSharedPreferences(
                "defaults",
                Application.MODE_PRIVATE
            )

    private val badge = Badge()

    val currentPageEditor = mutableStateOf<Editor?>(null)
    val currentTemplateChooser = mutableStateOf<TemplateChooser?>(null)
    val currentSimulatorSlot = mutableStateOf<Slot>(Slot.Name)
    val openApiKey = mutableStateOf(
        OPENAI_API_KEY.ifBlank {
            sharedPreferences.getString(OPEN_API_PREFERENCES_KEY, "")
        }
    )

    val slots = mutableStateOf(
        mutableMapOf(
            Slot.Name to initialConfiguration(Slot.Name),
            Slot.FirstSponsor to initialConfiguration(Slot.FirstSponsor),
            Slot.SecondSponsor to initialConfiguration(Slot.SecondSponsor),
            Slot.FirstCustom to initialConfiguration(Slot.FirstCustom),
            Slot.SecondCustom to initialConfiguration(Slot.SecondCustom),
        )
    )

    fun sendPageToDevice(slot: Slot) {
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

    fun customizeSlot(slot: Slot) {
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
                currentPageEditor.value = Editor(
                    slot,
                    slots.value[Slot.Name]!!
                )
            } else {
                Log.d("Customize Page", "Cannot configure slot '${slot.name}'.")
            }
        }
    }

    fun templateSelected(slot: Slot?, configuration: Configuration?) {
        currentTemplateChooser.value = null

        if (slot != null && configuration != null) {
            currentPageEditor.value = Editor(slot, configuration)
        }
    }

    fun slotConfigured(slot: Slot?, configuration: Configuration?) {
        currentPageEditor.value = null

        if (slot != null && configuration != null) {
            slots.value[slot] = configuration
            slot.save()
        }
    }

    fun simulatorButtonPressed(slot: Slot) {
        currentSimulatorSlot.value = slot
    }

    fun slotToBitmap(slot: Slot = currentSimulatorSlot.value): Bitmap =
        slots.value[slot]?.bitmap ?: R.drawable.error.toBitmap().also {
            Log.d("Slot to Bitmap", "Unavailable slot tried to fetch bitmap.")
        }

    fun resetSlot(slot: Slot) {
        slots.value[slot] = initialConfiguration(slot)
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
        }
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

    public fun saveAll() {
        for (slot in slots.value.keys) {
            slot.save()
        }
    }

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
}

private fun String?.isNeitherNullNorBlank(): Boolean = !this.isNullOrBlank()
