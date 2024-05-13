package de.berlindroid.zeapp.zeservices

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import de.berlindroid.zeapp.zeui.pixelBuffer
import de.berlindroid.zekompanion.BadgePayload
import de.berlindroid.zekompanion.Environment
import de.berlindroid.zekompanion.base64
import de.berlindroid.zekompanion.buildBadgeManager
import de.berlindroid.zekompanion.toBinary
import de.berlindroid.zekompanion.zipit
import javax.inject.Inject

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
    suspend fun listConfiguration(): Result<Map<String, String>> {
        val payload = BadgePayload(
            type = "config_list",
            meta = "",
            payload = "",
        )

        if (badgeManager.sendPayload(payload).isSuccess) {
            val responses = mutableListOf<Result<String>>()
            while (true) {
                val response = badgeManager.readResponse()
                if (response.isSuccess) {
                    responses += response
                } else {
                    break
                }
            }

            val successful = responses.filter { it.isSuccess }.joinToString(separator = "\n") {
                it.getOrDefault("")
            }

            print("message: '$successful'")
            val kv = mapOf(
                *successful.split(",").map {
                    val (k, v) = it.split(" = ")
                    k to v
                }.toTypedArray(),
            )
            return Result.success(kv)
        } else {
            return Result.failure(NoSuchElementException())
        }
    }

    /**
     * Update configuration on badge..
     */
    suspend fun updateConfiguration(configuration: Map<String, String>): Result<Int> {
        val config = configuration.entries.joinToString(separator = ",")

        val payload = BadgePayload(
            type = "config_update",
            meta = "",
            payload = config,
        )

        return badgeManager.sendPayload(payload)
    }

    fun isConnected(): Boolean = badgeManager.isConnected()
}
