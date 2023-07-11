package de.berlindroid.zeapp.zemodels

import android.graphics.Bitmap

enum class ZeBadgeType(val rawValue: String) {
    NAME("Name Tag"),
    QR_CODE("QRCode Tag"),
    CUSTOM_PICTURE("Custom Picture"),
    IMAGE_GEN("Image Gen"),
    GEOFENCE_SCHEDULE("Conference Schedule"),
    UPCOMING_WEATHER("Upcoming Weather"),
    RANDOM_QUOTE("Random quote"),
    KODEE("Kodee³"),
    CAMERA("Camera"),
    IMAGE_DRAWING("Image Drawing"),
    BARCODE_TAG("BarCode Tag"),
    PHRASE("Custom Phrase");

    companion object {
        fun getOrNull(type: String): ZeBadgeType? {
            return values().find { it.rawValue == type }
        }
    }
}

/**
 * The configuration of a slot
 *
 * Add your own configuration here if you want a new page.
 *
 * Every inheritor should contain a companion object TYPE field, so it's type can be retrieved
 * from saved places like shared preferences or the hardware badge.
 *
 * @param type the type used to store the value in saved places like shared preferences
 * @param humanTitle is the title to be used to interact with so called humans
 * @param bitmap the bitmap created, might be empty or an error bitmap at first
 */
sealed class ZeConfiguration(
    val type: ZeBadgeType,
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
        val name: String?,
        val contact: String?,
        override val bitmap: Bitmap,
    ) : ZeConfiguration(ZeBadgeType.NAME, humanTitle = ZeBadgeType.NAME.rawValue, bitmap)

    /**
     * Store the name and contact of an attendee.
     *
     * @param url the URL of the attendee github profile ("https://github.com/gdg-berlin-android")
     * @param bitmap (overriden) final page
     */
    data class QRCode(
        val title: String,
        val text: String,
        val url: String,
        val isVcard: Boolean,
        val phone: String,
        val email: String,
        override val bitmap: Bitmap,
    ) : ZeConfiguration(ZeBadgeType.QR_CODE, humanTitle = ZeBadgeType.QR_CODE.rawValue, bitmap)

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
    ) : ZeConfiguration(ZeBadgeType.CUSTOM_PICTURE, humanTitle = ZeBadgeType.CUSTOM_PICTURE.rawValue, bitmap)

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
    ) : ZeConfiguration(ZeBadgeType.IMAGE_GEN, humanTitle = ZeBadgeType.IMAGE_GEN.rawValue, bitmap)

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
    ) : ZeConfiguration(ZeBadgeType.GEOFENCE_SCHEDULE, humanTitle = ZeBadgeType.GEOFENCE_SCHEDULE.rawValue, bitmap)

    data class Weather(
        val date: String,
        val temperature: String,
        override val bitmap: Bitmap,
    ) : ZeConfiguration(ZeBadgeType.UPCOMING_WEATHER, humanTitle = ZeBadgeType.UPCOMING_WEATHER.rawValue, bitmap)

    data class Quote(
        val message: String,
        val author: String,
        override val bitmap: Bitmap,
    ) : ZeConfiguration(ZeBadgeType.RANDOM_QUOTE, humanTitle = ZeBadgeType.RANDOM_QUOTE.rawValue, bitmap)

    data class Kodee(
        override val bitmap: Bitmap,
    ) : ZeConfiguration(ZeBadgeType.KODEE, humanTitle = ZeBadgeType.KODEE.rawValue, bitmap)

    data class Camera(
        override val bitmap: Bitmap,
    ) : ZeConfiguration(ZeBadgeType.CAMERA, humanTitle = ZeBadgeType.CAMERA.rawValue, bitmap)

    /**
     * Configure this slot for image draw by user
     *
     * @param prompt describe the contents of the page to be created.
     * @param bitmap the resulting page.
     */
    data class ImageDraw(
        override val bitmap: Bitmap,
    ) : ZeConfiguration(ZeBadgeType.IMAGE_DRAWING, humanTitle = ZeBadgeType.IMAGE_DRAWING.rawValue, bitmap)

    /**
     * Store the name and contact of an attendee.
     *
     * @param url the URL of the attendee github profile ("https://github.com/gdg-berlin-android")
     * @param bitmap (overriden) final page
     */
    data class BarCode(
        val title: String,
        val url: String,
        override val bitmap: Bitmap,
    ) : ZeConfiguration(ZeBadgeType.BARCODE_TAG, humanTitle = ZeBadgeType.BARCODE_TAG.rawValue, bitmap)

    data class CustomPhrase(
        val phrase: String,
        override val bitmap: Bitmap
    ) : ZeConfiguration(ZeBadgeType.PHRASE, humanTitle = ZeBadgeType.PHRASE.rawValue, bitmap)

    // TODO: Add your own pages.
}
