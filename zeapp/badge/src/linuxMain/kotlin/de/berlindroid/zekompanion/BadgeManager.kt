package de.berlindroid.zekompanion

class LinuxBadgeManager : BadgeManager {
    override suspend fun sendPayload(payload: BadgePayload): Result<Int> {
        TODO("Not yet implemented")
    }

    override fun isConnected(): Boolean {
        TODO("Not yet implemented")
    }
}

actual fun buildBadgeManager(): BadgeManager = LinuxBadgeManager()
