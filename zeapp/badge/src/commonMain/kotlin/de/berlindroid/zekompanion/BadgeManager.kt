package de.berlindroid.zekompanion

/**
 * What to be send over to the badge?
 *
 * @param type what command should be executed
 * @param meta any meta information you want to receive back?
 * @param payload the payload of the command of type 'type'.
 */
sealed class BadgePayload(
    val debug: Boolean,
    val type: String,
    val meta: String,
    val payload: String,
) {
    /**
     * Convert the payload to a format the badge understands
     */
    fun toBadgeCommand(): String = "${if (debug) "debug:" else ""}$type:$meta:${payload}"

    class RawPayload(debug: Boolean = false, type: String, meta: String, payload: String) : BadgePayload(
        debug, type, meta, payload,
    )

    class HelpPayload(debug: Boolean = false) : BadgePayload(
        debug, type = "help", meta = "", payload = "",
    )

    class ReloadPayload(debug: Boolean = false) : BadgePayload(
        debug, type = "reload", meta = "", payload = "",
    )

    class ExitPayload(debug: Boolean = false) : BadgePayload(
        debug, type = "exit", meta = "", payload = "",
    )

    class TerminalPayload(debug: Boolean = false) : BadgePayload(
        debug, type = "terminal", meta = "", payload = "",
    )

    class RefreshPayload(debug: Boolean = false) : BadgePayload(
        debug, type = "refresh", meta = "", payload = "",
    )

    class ConfigSavePayload(debug: Boolean = false) : BadgePayload(
        debug, type = "config_save", meta = "", payload = "",
    )

    class ConfigLoadPayload(debug: Boolean = false) : BadgePayload(
        debug, type = "config_load", meta = "", payload = "",
    )

    class ConfigUpdatePayload(debug: Boolean = false, config: String) : BadgePayload(
        debug, type = "config_update", meta = "", payload = config,
    )

    class ConfigListPayload(debug: Boolean = false) : BadgePayload(
        debug, type = "config_list", meta = "", payload = "",
    )

    class ShowPayload(debug: Boolean = false, filename: String) : BadgePayload(
        debug, type = "show", meta = filename, payload = "",
    )

    class StorePayload(debug: Boolean = false, filename: String, payload: String) : BadgePayload(
        debug, type = "store", meta = filename, payload = payload,
    )

    class PreviewPayload(debug: Boolean = false, payload: String) : BadgePayload(
        debug, type = "preview", meta = "", payload = payload,
    )

    class ListPayload(debug: Boolean = false) : BadgePayload(
        debug, type = "list", meta = "", payload = "",
    )

    class DeletePayload(debug: Boolean = false, filename: String) : BadgePayload(
        debug, type = "delete", meta = filename, payload = "",
    )
}

interface BadgeManager {
    companion object {
        const val DEVICE_PRODUCT_NAME = "Badger 2040"
    }

    suspend fun sendPayload(payload: BadgePayload): Result<Int>

    suspend fun readResponse(): Result<String>

    fun isConnected(): Boolean
}

expect class Environment

expect fun buildBadgeManager(environment: Environment): BadgeManager
