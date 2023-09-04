package de.berlindroid.zekompanion

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPort.TIMEOUT_WRITE_BLOCKING
import kotlin.RuntimeException

class AppleBadgeManager : BadgeManager {
    override suspend fun sendPayload(payload: BadgePayload): Result<Int> = if (isConnected()) {
        val badgers = getBadger2040s()
        val bytes = payload.toBadgeCommand().toByteArray()

        var written = 0
        for (badger in badgers) {
            try {
                if (!badger.openPort(300)) {
                    continue
                }

                badger.setDTR()
                badger.baudRate = 115200
                badger.numDataBits = 8
                badger.numStopBits = 1
                badger.parity = 0

                badger.setComPortTimeouts(TIMEOUT_WRITE_BLOCKING, 2_0000, 2_000)

                badger.openPort()

                written = badger.writeBytes(bytes, bytes.size)
                badger.flushIOBuffers()

                if (written > 0) {
                    break
                }
            } catch (e: RuntimeException) {
                e.printStackTrace()
                written = -1
                break
            } finally {
                if (badger.isOpen) {
                    badger.closePort()
                }

                badger.clearDTR()
            }
        }

        if (written > 0) {
            Result.success(written)
        } else {
            Result.failure(RuntimeException("Couldn't write to badge"))
        }
    } else {
        Result.failure(RuntimeException("No badge connected."))
    }

    override fun isConnected(): Boolean {
        return getBadger2040s().isNotEmpty()
    }

    private fun getBadger2040s() = SerialPort.getCommPorts().filter {
        it.descriptivePortName.contains("Badger")
    }
}

actual typealias Environment = Any

actual fun buildBadgeManager(environment: Environment): BadgeManager = AppleBadgeManager()
