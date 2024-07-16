package de.berlindroid.zeapp.zerepositories

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import de.berlindroid.zeapp.zebits.toBitmap
import de.berlindroid.zeapp.zemodels.ZeBadgeType
import de.berlindroid.zeapp.zemodels.ZeConfiguration
import de.berlindroid.zeapp.zemodels.ZeSlot
import de.berlindroid.zeapp.zeservices.ZePreferencesService
import de.berlindroid.zeapp.zeservices.ZePreferencesService.Companion.IMAGE_KEY
import de.berlindroid.zeapp.zeservices.ZePreferencesService.Companion.TYPE_KEY
import de.berlindroid.zeapp.zeui.pixelBuffer
import de.berlindroid.zekompanion.BADGE_HEIGHT
import de.berlindroid.zekompanion.BADGE_WIDTH
import de.berlindroid.zekompanion.base64
import de.berlindroid.zekompanion.debase64
import de.berlindroid.zekompanion.toBinary
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapNotNull
import timber.log.Timber
import javax.inject.Inject

/**
 * Responsible for all the ZeSlot and ZeSlotConfiguration persistence
 */
class ZeSlotRepository @Inject constructor(
    private val zePreferencesService: ZePreferencesService,
) {
    suspend fun getSlotConfiguration(slot: ZeSlot): ZeConfiguration? {
        return zePreferencesService.dataStore.data.mapNotNull { preferences ->

            val type = ZeBadgeType.getOrNull(preferences[slot.preferencesKey(TYPE_KEY)].orEmpty())
            val bitmap =
                preferences[slot.preferencesKey(IMAGE_KEY)]
                    ?.debase64()
                    ?.toBitmap(BADGE_WIDTH, BADGE_HEIGHT)
                    ?: return@mapNotNull null

            when (type) {
                ZeBadgeType.NAME -> {
                    ZeConfiguration.Name(
                        name = preferences.slotFieldValue(slot, "name"),
                        contact = preferences.slotFieldValue(slot, "contact"),
                        bitmap = bitmap,
                    )
                }

                ZeBadgeType.CUSTOM_PICTURE -> ZeConfiguration.Picture(bitmap)

                ZeBadgeType.IMAGE_GEN ->
                    ZeConfiguration.ImageGen(
                        prompt = preferences.slotFieldValue(slot, "prompt"),
                        bitmap = bitmap,
                    )

                ZeBadgeType.GEOFENCE_SCHEDULE -> ZeConfiguration.Schedule(bitmap)

                ZeBadgeType.UPCOMING_WEATHER ->
                    ZeConfiguration.Weather(
                        date = preferences.slotFieldValue(slot, "weather_date"),
                        temperature = preferences.slotFieldValue(slot, "weather_temperature"),
                        bitmap,
                    )

                ZeBadgeType.QR_CODE ->
                    ZeConfiguration.QRCode(
                        title = preferences.slotFieldValue(slot, "qr_title"),
                        url = preferences.slotFieldValue(slot, "url"),
                        text = preferences.slotFieldValue(slot, "qr_text"),
                        isVcard = preferences.slotFieldValue(slot, "qr_is_vcard").toBoolean(),
                        phone = preferences.slotFieldValue(slot, "qr_phone"),
                        email = preferences.slotFieldValue(slot, "qr_email"),
                        bitmap = bitmap,
                    )

                ZeBadgeType.PHRASE ->
                    ZeConfiguration.CustomPhrase(
                        phrase = preferences.slotFieldValue(slot, "random_phrase"),
                        bitmap = bitmap,
                    )

                ZeBadgeType.BARCODE_TAG ->
                    ZeConfiguration.BarCode(
                        title = preferences.slotFieldValue(slot, "barcode_title"),
                        bitmap = bitmap,
                        url = preferences.slotFieldValue(slot, "url"),
                    )

                ZeBadgeType.RANDOM_QUOTE ->
                    ZeConfiguration.Quote(
                        message = preferences.slotFieldValue(slot, "quote_message"),
                        author = preferences.slotFieldValue(slot, "quote_author"),
                        bitmap = bitmap,
                    )

                ZeBadgeType.CAMERA -> ZeConfiguration.Camera(bitmap)

                else -> {
                    Timber.e("Slot from Prefs: Cannot find $type slot in preferences.")
                    null
                }
            }
        }.firstOrNull()
    }

    suspend fun saveSlotConfiguration(slot: ZeSlot, config: ZeConfiguration) {
        zePreferencesService.dataStore.edit { preferences ->
            preferences[slot.preferencesKey(TYPE_KEY)] = config.type.rawValue
            preferences[slot.preferencesKey(IMAGE_KEY)] = config.bitmap.pixelBuffer().toBinary().base64()

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

    suspend fun removeSlotConfiguration(slot: ZeSlot) {
        TODO("Not yet implemented")
    }

    fun getDefaultSlotConfiguration(slot: ZeSlot): ZeConfiguration {
        TODO("Not yet implemented")
    }

    fun getInitialSlots(): List<ZeSlot> {
        TODO("Not yet implemented")
    }
}


// Helper extension methods
private fun ZeSlot.preferencesKey(field: String): Preferences.Key<String> = stringPreferencesKey("slot.$name.$field")
private fun Preferences.slotFieldValue(slot: ZeSlot, field: String): String =
    slot.preferencesKey(field).let {
        if (this.contains(it)) {
            this[it]!!
        } else {
            ""
        }
    }


