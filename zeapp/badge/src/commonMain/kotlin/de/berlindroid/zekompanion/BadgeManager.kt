package de.berlindroid.zekompanion

/**
 * What to be send over to the badge?
 *
 * @param type what command should be executed
 * @param meta any meta information you want to receive back?
 * @param payload the payload of the command of type 'type'.
 */
data class BadgePayload(
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

interface BadgeManager {
    companion object {
        const val DEVICE_PRODUCT_NAME = "Badger 2040"
    }

    suspend fun sendPayload(payload: BadgePayload): Result<Int>

    fun isConnected(): Boolean
}

expect class Environment

expect fun buildBadgeManager(environment: Environment): BadgeManager
