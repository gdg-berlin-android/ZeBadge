package de.berlindroid.zeapp.vm

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.core.graphics.scale
import androidx.lifecycle.AndroidViewModel
import de.berlindroid.zeapp.PAGE_HEIGHT
import de.berlindroid.zeapp.PAGE_WIDTH
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.bits.isBinary
import de.berlindroid.zeapp.hardware.Badge

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
        ) : Configuration("Name Tag", bitmap)

        data class Schedule(
            override val bitmap: Bitmap,
        ) : Configuration("Conference Schedule", bitmap)

        data class Picture(
            override val bitmap: Bitmap,
        ) : Configuration("Custom Picture", bitmap)

        data class Weather(
            override val bitmap: Bitmap,
        ) : Configuration("Todays Weather", bitmap)
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

    val slots = mutableStateOf(
        mutableMapOf(
            Slot.Name to initialConfiguration(Slot.Name),
            Slot.FirstSponsor to initialConfiguration(Slot.FirstSponsor),
            Slot.SecondSponsor to initialConfiguration(Slot.SecondSponsor),
            Slot.FirstCustom to initialConfiguration(Slot.FirstCustom),
            Slot.SecondCustom to initialConfiguration(Slot.SecondCustom),
        )
    )

    fun sendPageToDevice(slot: Slot, bitmap: Bitmap) {
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
                configurations = listOf(
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
                )
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
        }
    }

    fun simulatorButtonPressed(slot: Slot) {
        currentSimulatorSlot.value = slot
    }

    fun slotToBitmap(slot: Slot = currentSimulatorSlot.value): Bitmap =
        slots.value[slot]?.bitmap ?: R.drawable.error.toBitmap().also {
            Log.d("Slot to Bitmap", "Unavailable slot tried to fetch bitmap")
        }

    fun resetSlot(slot: Slot) {
        slots.value[slot] = initialConfiguration(slot)
    }

    private fun initialConfiguration(slot: Slot): Configuration = when (slot) {
        is Slot.Name -> Configuration.Name("Your Name", "Your Contact", initialNameBitmap())
        is Slot.FirstSponsor -> Configuration.Picture(R.drawable.page_google.toBitmap())
        is Slot.SecondSponsor -> Configuration.Picture(R.drawable.page_telekom.toBitmap())
        is Slot.FirstCustom -> Configuration.Picture(R.drawable.soon.toBitmap())
        is Slot.SecondCustom -> Configuration.Picture(R.drawable.soon.toBitmap())
    }

    private fun initialNameBitmap(): Bitmap =
        BitmapFactory.decodeResource(
            getApplication<Application>().resources,
            R.drawable.sample_badge,
        ).scale(PAGE_WIDTH, PAGE_HEIGHT)

    private fun Int.toBitmap(): Bitmap =
        BitmapFactory.decodeResource(
            getApplication<Application>().resources,
            this,
        ).scale(PAGE_WIDTH, PAGE_HEIGHT)
}
