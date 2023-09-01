package de.berlindroid.zekompanion

class AppleBadgeManager : BadgeManager {
    override suspend fun sendPayload(payload: BadgePayload): Result<Int> {
        TODO("Not yet implemented")
    }

    override fun isConnected(): Boolean = false
}

actual typealias Environment = Any

actual fun buildBadgeManager(environment: Environment): BadgeManager = AppleBadgeManager()
