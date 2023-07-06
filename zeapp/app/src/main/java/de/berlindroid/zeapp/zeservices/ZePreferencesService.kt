package de.berlindroid.zeapp.zeservices

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import de.berlindroid.zeapp.zebits.base64
import de.berlindroid.zeapp.zebits.debase64
import de.berlindroid.zeapp.zebits.toBinary
import de.berlindroid.zeapp.zebits.toBitmap
import de.berlindroid.zeapp.zemodels.ZeConfiguration
import de.berlindroid.zeapp.zemodels.ZeSlot
import de.berlindroid.zeapp.zevm.ZeBadgeViewModel
import javax.inject.Inject

class ZePreferencesService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val OPEN_API_PREFERENCES_KEY = "openapi"
    }

    private val sharedPreferences by lazy {
        context.getSharedPreferences(
            "defaults",
            Application.MODE_PRIVATE,
        )
    }

    fun getOpenApiKey(): String {
        return sharedPreferences.getString(OPEN_API_PREFERENCES_KEY, null).orEmpty()
    }

    fun isSlotConfigured(slot: ZeSlot): Boolean {
        return sharedPreferences.contains(slot.preferencesTypeKey())
    }

    fun saveSlotConfiguration(slot: ZeSlot, config: ZeConfiguration) {
        sharedPreferences.edit {
            putString(slot.preferencesTypeKey(), config.type)
            putString(slot.preferencesBitmapKey(), config.bitmap.toBinary().base64())

            when (config) {
                is ZeConfiguration.Name -> {
                    putString(slot.preferencesKey("name"), config.name)
                    putString(slot.preferencesKey("contact"), config.contact)
                }

                is ZeConfiguration.ImageGen -> {
                    putString(slot.preferencesKey("prompt"), config.prompt)
                }

                is ZeConfiguration.Picture -> {
                    // Nothing more to configure
                }

                is ZeConfiguration.Schedule -> {
                    // TODO: Save schedule
                }

                is ZeConfiguration.Weather -> {
                    putString(slot.preferencesKey("weather_date"), config.date)
                    putString(slot.preferencesKey("weather_temperature"), config.temperature)
                }

                is ZeConfiguration.QRCode -> {
                    putString(slot.preferencesKey("qr_title"), config.title)
                    putString(slot.preferencesKey("url"), config.url)
                    putString(slot.preferencesKey("qr_text"), config.text)
                }

                is ZeConfiguration.Camera,
                is ZeConfiguration.Kodee -> Unit

                is ZeConfiguration.ImageDraw -> {
                    // Nothing more to configure
                }

                is ZeConfiguration.Quote -> {
                    putString(slot.preferencesKey("quote_author"), config.author)
                    putString(slot.preferencesKey("quote_message"), config.message)
                }

                is ZeConfiguration.BarCode -> {
                    putString(slot.preferencesKey("barcode_title"), config.title)
                    putString(slot.preferencesKey("url"), config.url)
                }
            }
        }
    }

    fun getSlotConfiguration(slot: ZeSlot): ZeConfiguration? {
        val type = slot.preferencesType()
        val bitmap = slot.preferencesBitmap()

        return when (type) {
            ZeConfiguration.Name.TYPE -> {
                ZeConfiguration.Name(
                    name = slot.preferencesValue("name"),
                    contact = slot.preferencesValue("contact"),
                    bitmap = bitmap,
                )
            }

            ZeConfiguration.Picture.TYPE -> ZeConfiguration.Picture(bitmap)

            ZeConfiguration.ImageGen.TYPE -> ZeConfiguration.ImageGen(
                prompt = slot.preferencesValue("prompt"),
                bitmap = bitmap
            )

            ZeConfiguration.Schedule.TYPE -> ZeConfiguration.Schedule(bitmap)

            ZeConfiguration.Weather.TYPE -> ZeConfiguration.Weather(
                date = slot.preferencesValue("weather_date"),
                temperature = slot.preferencesValue("weather_temperature"),
                bitmap
            )

            ZeConfiguration.QRCode.TYPE -> ZeConfiguration.QRCode(
                title = slot.preferencesValue("qr_title"),
                url = slot.preferencesValue("url"),
                text = slot.preferencesValue("qr_text"),
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

    private fun ZeSlot.preferencesTypeKey(): String = preferencesKey("type")

    private fun ZeSlot.preferencesType(): String = preferencesValue("type")

    private fun ZeSlot.preferencesBitmapKey(): String = preferencesKey("bitmap")

    private fun ZeSlot.preferencesBitmap(): Bitmap = preferencesValue("bitmap")
        .debase64()
        .toBitmap()

    private fun ZeSlot.preferencesKey(field: String): String =
        "slot.$name.$field"

    private fun ZeSlot.preferencesValue(field: String): String =
        sharedPreferences.getString(preferencesKey(field), "")
            .orEmpty()
}
