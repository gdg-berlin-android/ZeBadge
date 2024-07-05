package de.berlindroid.zekompanion

/**
 * What to be send over to the badge?
 *
 * @param type what command should be executed
 * @param meta any meta information you want to receive back?
 * @param payload the payload of the command of type 'type'.
 */
open class BadgePayload(
    open val debug: Boolean = false,
    val type: String,
    open val meta: String,
    open val payload: String,
) {
    /**
     * Convert the payload to a format the badge understands
     */
    fun toBadgeCommand(): String = "${if (debug) "debug:" else ""}$type:$meta:${payload}"
}

class HelpCommand(
    override val debug: Boolean = false,
    override val meta: String = "",
    override val payload: String = "",
): BadgePayload(
    debug = debug,
    type = "help",
    meta = meta,
    payload = payload,
)

data class ReloadCommand(
    override val debug: Boolean = false,
    override val meta: String = "",
    override val payload: String = "",
): BadgePayload(
    debug = debug,
    type = "reload",
    meta = meta,
    payload = payload,
)

data class ExitCommand(
    override val debug: Boolean = false,
    override val meta: String = "",
    override val payload: String = "",
): BadgePayload(
    debug = debug,
    type = "exit",
    meta = meta,
    payload = payload,
)

data class TerminalCommand(
    override val debug: Boolean = false,
    override val meta: String = "",
    override val payload: String = "",
): BadgePayload(
    debug = debug,
    type = "terminal",
    meta = meta,
    payload = payload,
)

data class RefreshCommand(
    override val debug: Boolean = false,
    override val meta: String = "",
    override val payload: String = "",
): BadgePayload(
    debug = debug,
    type = "refresh",
    meta = meta,
    payload = payload,
)

data class ConfigSaveCommand(
    override val debug: Boolean = false,
    override val meta: String = "",
    override val payload: String = "",
): BadgePayload(
    debug = debug,
    type = "config_save",
    meta = meta,
    payload = payload,
)

data class ConfigCommand(
    override val debug: Boolean = false,
    override val meta: String = "",
    override val payload: String = "",
) : BadgePayload(
    debug = debug,
    type = "config_load",
    meta = meta,
    payload = payload,
)

data class ConfigUpdateCommand(
    override val debug: Boolean = false,
    override val meta: String = "",
    override val payload: String = "",
) : BadgePayload(
    debug = debug,
    type = "config_update",
    meta = meta,
    payload = payload,
)

data class ConfigListCommand(
    override val debug: Boolean = false,
    override val meta: String = "",
    override val payload: String = "",
) : BadgePayload(
    debug = debug,
    type = "config_list",
    meta = meta,
    payload = payload,
)

data class ShowCommand(
    override val debug: Boolean = false,
    override val meta: String = "",
    override val payload: String = "",
) : BadgePayload(
    debug = debug,
    type = "show",
    meta = meta,
    payload = payload,
)

data class StoreCommand(
    override val debug: Boolean = false,
    override val meta: String = "",
    override val payload: String = "",
) : BadgePayload(
    debug = debug,
    type = "store",
    meta = meta,
    payload = payload,
)

data class PreviewCommand(
    override val debug: Boolean = false,
    override val meta: String = "",
    override val payload: String = "",
): BadgePayload(
    debug = debug,
    type = "preview",
    meta = meta,
    payload = payload,
)

data class ListCommand(
    override val debug: Boolean = false,
    override val meta: String = "",
    override val payload: String = "",
) : BadgePayload(
    debug = debug,
    type = "list",
    meta = meta,
    payload = payload,
)

data class DeleteCommand(
    override val debug: Boolean = false,
    override val meta: String = "",
    override val payload: String = "",
) : BadgePayload(
    debug = debug,
    type = "delete",
    meta = meta,
    payload = payload,
)

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
