package de.berlindroid.zeapp.zerepositories

import android.graphics.Bitmap
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.zebits.toBitmap
import de.berlindroid.zeapp.zemodels.ZeBadgeType
import de.berlindroid.zeapp.zemodels.ZeConfiguration
import de.berlindroid.zeapp.zemodels.ZeSlot
import de.berlindroid.zeapp.zeservices.ZeImageProviderService
import de.berlindroid.zeapp.zeservices.ZePreferencesService
import de.berlindroid.zeapp.zeui.pixelBuffer
import de.berlindroid.zekompanion.BADGE_HEIGHT
import de.berlindroid.zekompanion.BADGE_WIDTH
import de.berlindroid.zekompanion.base64
import de.berlindroid.zekompanion.debase64
import de.berlindroid.zekompanion.toBinary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Responsible for all the ZeSlot and ZeSlotConfiguration persistence
 * It provides easy access methods to
 * [getSlotConfiguration], [saveSlotConfiguration], [removeSlotConfiguration], [getDefaultSlotConfiguration] and [getInitialSlots]
 */
class ZeSlotRepository(
    private val zePreferencesService: ZePreferencesService,
    private val imageProviderService: ZeImageProviderService,
) {
    // Constants
    private companion object {
        const val TYPE_KEY = "type"
        const val IMAGE_KEY = "bitmap"
    }

    suspend fun getSlotConfiguration(slot: ZeSlot): ZeConfiguration? =
        withContext(Dispatchers.IO) {
            return@withContext zePreferencesService.dataStore.data.map { preferences ->

                val type = ZeBadgeType.getOrNull(preferences[slot.preferencesKey(TYPE_KEY)].orEmpty())
                val bitmap =
                    preferences[slot.preferencesKey(IMAGE_KEY)]
                        ?.debase64()
                        ?.toBitmap(BADGE_WIDTH, BADGE_HEIGHT)
                        ?: return@map null

                return@map getSlotConfigurationByType(
                    slot = slot,
                    type = type,
                    bitmap = bitmap,
                    preferences = preferences,
                )
            }.firstOrNull()
        }

    suspend fun saveSlotConfiguration(
        slot: ZeSlot,
        config: ZeConfiguration,
    ) {
        zePreferencesService.dataStore.edit { preferences ->
            preferences[slot.preferencesKey(TYPE_KEY)] = config.type.rawValue
            preferences[slot.preferencesKey(IMAGE_KEY)] = config.bitmap.pixelBuffer().toBinary().base64()

            saveSlotConfigurationByConfig(
                slot = slot,
                config = config,
                preferences = preferences,
            )
        }
    }

    suspend fun removeSlotConfiguration(slot: ZeSlot) {
        zePreferencesService.dataStore.edit { mutablePreferences ->
            // Getting all related information about the slot config.
            val slotRelatedKeys =
                mutablePreferences.asMap().keys.filter {
                    it.name.contains(slot.preferencesKeyPrefix())
                }

            // Deleting each key that refers to that slot config.
            slotRelatedKeys.forEach { mutablePreferences.remove(it) }
        }
    }

    suspend fun getDefaultSlotConfiguration(slot: ZeSlot): ZeConfiguration =
        withContext(Dispatchers.IO) {
            when (slot) {
                is ZeSlot.Name ->
                    ZeConfiguration.Name(
                        null,
                        null,
                        imageProviderService.getInitialNameBitmap(),
                    )

                is ZeSlot.FirstSponsor -> ZeConfiguration.Picture(R.drawable.page_google.toBitmap())
                is ZeSlot.FirstCustom -> ZeConfiguration.Picture(R.drawable.soon.toBitmap())
                is ZeSlot.SecondCustom -> ZeConfiguration.Picture(R.drawable.soon.toBitmap())
                ZeSlot.QRCode ->
                    ZeConfiguration.QRCode(
                        title = "",
                        text = "",
                        url = "",
                        isVcard = false,
                        phone = "",
                        email = "",
                        bitmap = R.drawable.qrpage_preview.toBitmap(),
                    )

                ZeSlot.Weather ->
                    ZeConfiguration.Weather(
                        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                        "22C",
                        R.drawable.soon.toBitmap(),
                    )

                is ZeSlot.Quote ->
                    ZeConfiguration.Quote(
                        "Test",
                        "Author",
                        R.drawable.page_quote_sample.toBitmap(),
                    )

                ZeSlot.BarCode ->
                    ZeConfiguration.BarCode(
                        "Your title for barcode",
                        "",
                        R.drawable.soon.toBitmap(),
                    )

                ZeSlot.Add ->
                    ZeConfiguration.Name(
                        null,
                        null,
                        imageProviderService.provideImageBitmap(R.drawable.add),
                    )

                ZeSlot.Camera ->
                    ZeConfiguration.Camera(
                        imageProviderService.provideImageBitmap(R.drawable.soon),
                    )
            }
        }

    fun getInitialSlots(): List<ZeSlot> =
        listOf(
            ZeSlot.Name,
            ZeSlot.FirstSponsor,
            ZeSlot.Camera,
            ZeSlot.Add,
        )

    // region private implementation methods
    private fun getSlotConfigurationByType(
        slot: ZeSlot,
        type: ZeBadgeType?,
        bitmap: Bitmap,
        preferences: Preferences,
    ) = when (type) {
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

    private fun saveSlotConfigurationByConfig(
        slot: ZeSlot,
        config: ZeConfiguration,
        preferences: MutablePreferences,
    ) {
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

    private fun Int.toBitmap(): Bitmap = imageProviderService.provideImageBitmap(this)
    //endregion
}

// Helper extension methods
private fun ZeSlot.preferencesKeyPrefix(): String = "slot.$name"

private fun ZeSlot.preferencesKey(field: String): Preferences.Key<String> = stringPreferencesKey("${this.preferencesKeyPrefix()}.$field")

private fun Preferences.slotFieldValue(
    slot: ZeSlot,
    field: String,
): String =
    slot.preferencesKey(field).let {
        if (this.contains(it)) {
            this[it]!!
        } else {
            ""
        }
    }
