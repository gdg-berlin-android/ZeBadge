package de.berlindroid.zeapp.zeservices

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import de.berlindroid.zeapp.zebits.base64
import de.berlindroid.zeapp.zebits.toBinary
import de.berlindroid.zeapp.zebits.zipit
import de.berlindroid.zekompanion.BadgePayload
import de.berlindroid.zekompanion.Environment
import de.berlindroid.zekompanion.buildBadgeManager
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
        val payload = BadgePayload(
            type = "preview",
            meta = "",
            payload = page.toBinary().zipit().base64(),
        )

        return badgeManager.sendPayload(payload)
    }

    fun isConnected(): Boolean = badgeManager.isConnected()
}
