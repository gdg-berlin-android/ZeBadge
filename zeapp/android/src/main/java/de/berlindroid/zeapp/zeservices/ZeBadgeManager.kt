package de.berlindroid.zeapp.zeservices

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import de.berlindroid.zeapp.zeui.pixelBuffer
import de.berlindroid.zekompanion.BadgePayload
import de.berlindroid.zekompanion.Environment
import de.berlindroid.zekompanion.base64
import de.berlindroid.zekompanion.buildBadgeManager
import de.berlindroid.zekompanion.toBinary
import de.berlindroid.zekompanion.zipit
import javax.inject.Inject
import timber.log.Timber

private val SPACE_REPLACEMENT = "\$SPACE#"

class ZeBadgeManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val badgeManager = buildBadgeManager(Environment(context))

    /**
     * Send a bitmap to the badge to be shown instantaneous
     *
     * @param page the bitmap in black / white to be send to the badge
     */
    suspend fun previewPage(page: Bitmap): Result<Int> {
        val binaryPayload = page
            .pixelBuffer()
            .toBinary()
            .zipit()
            .base64()

        val payload = BadgePayload(
            type = "preview",
            meta = "",
            payload = binaryPayload,
        )

        return badgeManager.sendPayload(payload)
    }

    /**
     * Store a bitmap on the badge
     *
     * @param name a file name on the badge to be stored
     * @param page the bitmap in black / white to be send to the badge
     */
    suspend fun storePage(name: String, page: Bitmap): Result<Int> {
        val binaryPayload = page
            .pixelBuffer()
            .toBinary()
            .zipit()
            .base64()

        val payload = BadgePayload(
            type = "store",
            meta = name,
            payload = binaryPayload,
        )

        return badgeManager.sendPayload(payload)
    }

    /**
     * Return the name of the pages stored on the badge.
     */
    suspend fun requestPagesStored(): Result<String> {
        val payload = BadgePayload(
            type = "list",
            meta = "",
            payload = "",
        )

        if (badgeManager.sendPayload(payload).isSuccess) {
            return badgeManager.readResponse()
        } else {
            return Result.failure(NoSuchElementException())
        }
    }

    /**
     * Return the current active configuration.
     */
    suspend fun listConfiguration(): Result<Map<String, Any?>> {
        val payload = BadgePayload(
            type = "config_list",
            meta = "",
            payload = "",
        )

        if (badgeManager.sendPayload(payload).isSuccess) {
            val response = badgeManager.readResponse()
            if (response.isSuccess) {
                val config = response.getOrDefault("")
                Timber.v("Badge sent response: successfully received configuration: '${config.replace("\n", "\\n")}'.")

                val kv = mapOf(
                    *config.split(" ").mapNotNull {
                        if ("=" in it) {
                            val (key, value) = it.split("=")
                            val typedValue = pythonToKotlin(value)
                            key to typedValue
                        } else {
                            Timber.v("Config '$it' is malformed, ignoring it.")
                            null
                        }
                    }.toTypedArray(),
                )
                return Result.success(kv)
            }
            return Result.failure(IllegalStateException())
        } else {
            return Result.failure(NoSuchElementException())
        }
    }

    /**
     * Update configuration on badge..
     */
    suspend fun updateConfiguration(configuration: Map<String, Any?>): Result<Any> {

        val detypedConfig: Map<String, String> = configuration.map { e ->
            val (k, v) = e
            k to kotlinToPython(v)
        }.toMap()

        val config = detypedConfig.entries.joinToString(separator = " ")

        val payload = BadgePayload(
            type = "config_update",
            meta = "",
            payload = config,
        )

        if (badgeManager.sendPayload(payload).isSuccess) {

            if (badgeManager.sendPayload(
                    BadgePayload(
                        type = "config_save",
                        meta = "",
                        payload = "",
                    ),
                ).isSuccess
            ) {
                return Result.success(true)
            } else {
                return Result.failure(IllegalStateException())
            }
        } else {
            return Result.failure(NoSuchElementException())
        }
    }

    fun isConnected(): Boolean = badgeManager.isConnected()
}

private fun pythonToKotlin(value: String): Any? = when {
    value.startsWith("\"") -> {
        value
            .replace("\"", "")
            .replace(SPACE_REPLACEMENT, " ")
    }

    value.startsWith("\'") -> {
        value
            .replace("\'", "")
            .replace(SPACE_REPLACEMENT, " ")
    }

    value == "None" -> null
    value.toIntOrNull() != null -> value.toInt()
    value.toFloatOrNull() != null -> value.toFloat()
    value == "True" -> true
    value == "False" -> false
    else -> value
}

private fun kotlinToPython(value: Any?): String = when (value) {
    null -> "None"
    is String -> "\"${value.replace(" ", SPACE_REPLACEMENT)}\""
    is Boolean -> if (value) "True" else "False"
    else -> "$value"
}
