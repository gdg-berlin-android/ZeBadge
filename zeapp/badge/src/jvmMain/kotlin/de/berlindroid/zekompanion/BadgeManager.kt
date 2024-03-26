package de.berlindroid.zekompanion

import com.fazecast.jSerialComm.SerialPort
import java.lang.Thread.sleep
import kotlin.IllegalStateException

abstract class JvmBadgeManager : BadgeManager {
    override suspend fun sendPayload(payload: BadgePayload): Result<Int> = if (isConnected()) {
        val badgers = getBadger2040s()
        val command = payload.toBadgeCommand()
        val commandBytes = command.toByteArray(Charsets.UTF_8)

        var written = 0
        for (badger in badgers) {
            try {
                if (!badger.openPort(300)) {
                    // couldn't open, try next one
                    continue
                }
                setupPort(badger)

                // write as much as you can and then write some more
                var offset = 0
                while (offset < commandBytes.size) {
                    val length = commandBytes.size - offset
                    offset += badger.writeBytes(commandBytes, length, offset)
                }

                written = offset

                // wait some more time for the buffers to recover
                sleep(300)

                if (written > 0) {
                    break
                }
            } catch (e: RuntimeException) {
                written = -1
                break
            } finally {
                badger.flushIOBuffers()
                if (badger.isOpen) {
                    badger.closePort()
                }
                badger.clearDTR()
            }
        }

        if (written > 0) {
            Result.success(written)
        } else {
            Result.failure(RuntimeException("Couldn't write to badge."))
        }
    } else {
        Result.failure(RuntimeException("No badge connected."))
    }

    private fun setupPort(badger: SerialPort) {
        if (!badger.setDTR()) {
            throw IllegalStateException("Could not set dtr on $badger.")
        }

        if (!badger.setBaudRate(115200)) {
            throw IllegalStateException("Could not set baud rate on $badger.")
        }

        if (!badger.setNumDataBits(8)) {
            throw IllegalStateException("Could not data bit on $badger.")
        }

        if (!badger.setNumStopBits(1)) {
            throw IllegalStateException("Could not set num stop bits on $badger.")
        }

        if (!badger.setParity(SerialPort.NO_PARITY)) {
            throw IllegalStateException("Could not set parity bit on $badger.")
        }
    }

    override fun isConnected(): Boolean {
        return getBadger2040s().isNotEmpty()
    }

    abstract fun getBadger2040s() : List<SerialPort>
}
