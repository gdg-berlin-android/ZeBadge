package de.berlindroid.zeapp.zemodels

/**
 * What to be send over to the badge?
 *
 * @param type what command should be executed
 * @param meta any meta information you want to receive back?
 * @param payload the payload of the command of type 'type'.
 */
data class ZeBadgePayload(
    val debug: Boolean = false,
    val type: String,
    val meta: String,
    val payload: String,
) {
    /**
     * Convert the payload to a format the badge understands
     */
    fun toBadgeCommand(): String = "${if (debug) "debug:" else ""}$type:$meta:$payload"
}
