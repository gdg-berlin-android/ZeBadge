package de.berlindroid.zeapp.zemodels

import android.graphics.Bitmap
import de.berlindroid.zeapp.zevm.ZeBadgeViewModel

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
    val type: String,
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
    ) : ZeConfiguration(TYPE, humanTitle = "Name Tag", bitmap) {
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
        val text: String,
        val url: String,
        override val bitmap: Bitmap,
    ) : ZeConfiguration(TYPE, humanTitle = "QRCode Tag", bitmap) {
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
    ) : ZeConfiguration(TYPE, humanTitle = "Custom Picture", bitmap) {
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
    ) : ZeConfiguration(TYPE, humanTitle = "Image Gen", bitmap) {
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
    ) : ZeConfiguration(TYPE, humanTitle = "Conference Schedule", bitmap) {
        companion object {
            const val TYPE: String = "Conference Schedule"
        }
    }

    data class Weather(
        val date: String,
        val temperature: String,
        override val bitmap: Bitmap,
    ) : ZeConfiguration(TYPE, humanTitle = "Upcoming Weather", bitmap) {
        companion object {
            const val TYPE: String = "Upcoming Weather"
        }
    }

    data class Quote(
        val message: String,
        val author: String,
        override val bitmap: Bitmap,
    ) : ZeConfiguration(TYPE, humanTitle = "Random quotes", bitmap) {
        companion object {
            const val TYPE: String = "Random quotes"
        }
    }

    data class Kodee(
        override val bitmap: Bitmap,
    ) : ZeConfiguration(TYPE, humanTitle = "Kodee³", bitmap) {
        companion object {
            const val TYPE: String = "Kodee"
        }
    }

    data class Camera(
        override val bitmap: Bitmap,
    ) : ZeConfiguration(TYPE, humanTitle = "Camera", bitmap) {
        companion object {
            const val TYPE: String = "Camera"
        }
    }

    /**
     * Configure this slot for image draw by user
     *
     * @param prompt describe the contents of the page to be created.
     * @param bitmap the resulting page.
     */
    data class ImageDraw(
        override val bitmap: Bitmap,
    ) : ZeConfiguration(TYPE, humanTitle = "Image Draw", bitmap) {
        companion object {
            const val TYPE: String = "Image Draw"
        }
    }

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
    ) : ZeConfiguration(TYPE, humanTitle = "BarCode Tag", bitmap) {
        companion object {
            const val TYPE: String = "BarCode Tag"
        }
    }

    // TODO: Add your own pages.
}