package de.berlindroid.zeapp.zeservices

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import de.berlindroid.zeapp.zebits.base64
import de.berlindroid.zeapp.zebits.debase64
import de.berlindroid.zeapp.zebits.toBinary
import de.berlindroid.zeapp.zebits.toBitmap
import de.berlindroid.zeapp.zemodels.ZeConfiguration
import de.berlindroid.zeapp.zemodels.ZeSlot
import timber.log.Timber
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

private const val PREFS_NAME = "defaults"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREFS_NAME,
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, PREFS_NAME))
    },
)

class ZePreferencesService @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private companion object {
        val OPEN_API_PREFERENCES_KEY = stringPreferencesKey("openapi")
        const val TYPE_KEY = "type"
        const val IMAGE_KEY = "bitmap"
    }

    private val dataStore = context.dataStore

    suspend fun getOpenApiKey(): String {
        return dataStore.data.map { preferences -> preferences[OPEN_API_PREFERENCES_KEY].orEmpty() }
            .first()
    }

    suspend fun isSlotConfigured(slot: ZeSlot): Boolean {
        return dataStore.data.map { preferences -> preferences.contains(slot.preferencesKey(TYPE_KEY)) }
            .first()
    }

    suspend fun saveSlotConfiguration(slot: ZeSlot, config: ZeConfiguration) {
        dataStore.edit { preferences ->
            preferences[slot.preferencesKey(TYPE_KEY)] = config.type
            preferences[slot.preferencesKey(IMAGE_KEY)] = config.bitmap.toBinary().base64()

            when (config) {
                is ZeConfiguration.Name -> {
                    preferences[slot.preferencesKey("name")] = config.name.orEmpty()
                    preferences[slot.preferencesKey("contact")] = config.contact.orEmpty()
                }

                is ZeConfiguration.ImageGen -> {
                    preferences[slot.preferencesKey("prompt")] = config.prompt
                }

                is ZeConfiguration.Picture -> {
                    // Nothing more to configure
                }

                is ZeConfiguration.Schedule -> {
                    // TODO: Save schedule
                }

                is ZeConfiguration.Weather -> {
                    preferences[slot.preferencesKey("weather_date")] = config.date
                    preferences[slot.preferencesKey("weather_temperature")] = config.temperature
                }

                is ZeConfiguration.QRCode -> {
                    preferences[slot.preferencesKey("qr_title")] = config.title
                    preferences[slot.preferencesKey("url")] = config.url
                    preferences[slot.preferencesKey("qr_text")] = config.text
                    preferences[slot.preferencesKey("qr_phone")] = config.phone
                    preferences[slot.preferencesKey("qr_email")] = config.email
                    preferences[slot.preferencesKey("qr_is_vcard")] = config.isVcard.toString()
                }

                is ZeConfiguration.Camera,
                is ZeConfiguration.Kodee,
                -> Unit

                is ZeConfiguration.ImageDraw -> {
                    // Nothing more to configure
                }

                is ZeConfiguration.Quote -> {
                    preferences[slot.preferencesKey("quote_author")] = config.author
                    preferences[slot.preferencesKey("quote_message")] = config.message
                }

                is ZeConfiguration.BarCode -> {
                    preferences[slot.preferencesKey("barcode_title")] = config.title
                    preferences[slot.preferencesKey("url")] = config.url
                }

                is ZeConfiguration.CustomPhrase -> {
                    preferences[slot.preferencesKey("random_phrase")] = config.phrase
                }
            }
        }
    }

    suspend fun getSlotConfiguration(slot: ZeSlot): ZeConfiguration? {
        return dataStore.data.mapNotNull { preferences ->

            val type = preferences[slot.preferencesKey(TYPE_KEY)]
            val bitmap = preferences[slot.preferencesKey(IMAGE_KEY)]?.debase64()?.toBitmap() ?: return@mapNotNull null

            when (type) {
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
                    bitmap = bitmap,
                )

                ZeConfiguration.Schedule.TYPE -> ZeConfiguration.Schedule(bitmap)

                ZeConfiguration.Weather.TYPE -> ZeConfiguration.Weather(
                    date = slot.preferencesValue("weather_date"),
                    temperature = slot.preferencesValue("weather_temperature"),
                    bitmap,
                )

                ZeConfiguration.QRCode.TYPE -> ZeConfiguration.QRCode(
                    title = slot.preferencesValue("qr_title"),
                    url = slot.preferencesValue("url"),
                    text = slot.preferencesValue("qr_text"),
                    isVcard = slot.preferencesBooleanValue("qr_is_vcard"),
                    phone = slot.preferencesValue("qr_phone"),
                    email = slot.preferencesValue("qr_email"),
                    bitmap = bitmap,
                )

                ZeConfiguration.CustomPhrase.TYPE -> ZeConfiguration.CustomPhrase(
                    phrase = slot.preferencesValue("random_phrase"),
                    bitmap = bitmap,
                )

                ZeConfiguration.BarCode.TYPE -> ZeConfiguration.BarCode(
                    title = slot.preferencesValue("barcode_title"),
                    bitmap = bitmap,
                    url = slot.preferencesValue("url"),
                )

                else -> {
                Timber.e(
                        "Slot from Prefs",
                        "Cannot find $type slot in preferences.",
                    )
                    null
                }
            }
        }.firstOrNull()
    }

    private fun ZeSlot.preferencesKey(field: String): Preferences.Key<String> =
        stringPreferencesKey("slot.$name.$field")

    private suspend fun ZeSlot.preferencesValue(field: String): String =
        dataStore.data.map { preferences ->
            val key = preferencesKey(field)
            if (preferences.contains(key)) {
                preferences[key]!!
            } else {
                ""
            }
        }.first()

    private suspend fun ZeSlot.preferencesBooleanValue(field: String): Boolean =
        preferencesValue(field).toBoolean()
}
