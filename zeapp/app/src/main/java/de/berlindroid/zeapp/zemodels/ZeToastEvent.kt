package de.berlindroid.zeapp.zemodels

/**
 * An event that represents the display of a toast.
 *
 * @param message The message to be displayed in the toast
 * @param duration The [Duration] of the toast display
 */
data class ZeToastEvent(
    val message: String,
    val duration: Duration,
) {

    enum class Duration {
        SHORT,
        LONG,
    }
}
