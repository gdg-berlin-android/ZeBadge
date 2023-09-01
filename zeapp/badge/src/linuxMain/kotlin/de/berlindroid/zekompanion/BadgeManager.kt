package de.berlindroid.zekompanion

import java.io.File
import java.lang.RuntimeException

class AppleBadgeManager : BadgeManager {
    override suspend fun sendPayload(payload: BadgePayload): Result<Int> = if (isConnected()) {
        val serial = getUsbModems().last()
        val bytes = payload.toBadgeCommand().toByteArray()
        serial.writeBytes(bytes)
        Result.success(bytes.size)
    } else {
        Result.failure(RuntimeException("No badge connected."))
    }

    override fun isConnected(): Boolean {
        return getUsbModems().size == 2
    }

    private fun getUsbModems() = File("/dev/")
        .listFiles()
        .orEmpty()
        .filter {
            it.name.startsWith("ttyACM")
        }
}

actual typealias Environment = Any

actual fun buildBadgeManager(environment: Environment): BadgeManager = AppleBadgeManager()
