package de.berlindroid.zeapp.zeservices

import android.content.Context
import android.util.Log
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

private const val PREFS_NAME = "defaults"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREFS_NAME,
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, PREFS_NAME))
    })

class ZePreferencesService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private companion object {
        val OPEN_API_PREFERENCES_KEY = stringPreferencesKey("openapi")
        val TYPE_KEY = stringPreferencesKey("type")
        val IMAGE_KEY = stringPreferencesKey("bitmap")
    }
    private val dataStore = context.dataStore

    suspend fun getOpenApiKey(): String {
        return dataStore.data.map { preferences -> preferences[OPEN_API_PREFERENCES_KEY].orEmpty() }
            .first()
    }

    suspend fun isSlotConfigured(slot: ZeSlot): Boolean {
        return dataStore.data.map { preferences -> preferences.contains(TYPE_KEY) }
            .first()
    }

    suspend fun saveSlotConfiguration(slot: ZeSlot, config: ZeConfiguration) {
        dataStore.edit { preferences ->
            preferences[TYPE_KEY] = config.type
            preferences[IMAGE_KEY] = config.bitmap.toBinary().base64()

            when (config) {
                is ZeConfiguration.Name -> {
                    preferences[slot.preferencesKey("name")] = config.name
                    preferences[slot.preferencesKey("contact")] = config.contact
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
                }

                is ZeConfiguration.Camera,
                is ZeConfiguration.Kodee -> Unit

                is ZeConfiguration.ImageDraw -> {
                    // Nothing more to configure
                }

                is ZeConfiguration.BarCode -> {
                    preferences[slot.preferencesKey("barcode_title")] = config.title
                    preferences[slot.preferencesKey("url")] = config.url
                }
            }
        }
    }

    suspend fun getSlotConfiguration(slot: ZeSlot): ZeConfiguration? {
        return dataStore.data.mapNotNull { preferences ->

            val type = preferences[TYPE_KEY]
            val bitmap = preferences[IMAGE_KEY]?.debase64()?.toBitmap()?: return@mapNotNull null

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
        }.first()
    }

    private fun ZeSlot.preferencesKey(field: String): Preferences.Key<String> =
        stringPreferencesKey("slot.$name.$field")

    private suspend fun ZeSlot.preferencesValue(field: String): String =
        dataStore.data.map { preferences ->
            val key = preferencesKey(field)
            if (preferences.contains(key)) {
                preferences[key]!!
            } else ""
        }.first()
}
