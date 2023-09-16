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
     * Send a bitmap to the badge for the name slot
     *
     * @param name the name of the page / slot for the bitmap
     * @param page the bitmap in black / white to be send to the badge
     */
    suspend fun sendPage(name: String, page: Bitmap): Result<Int> {
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

    fun isConnected(): Boolean = badgeManager.isConnected()
}
