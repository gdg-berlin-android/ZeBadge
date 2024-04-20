package de.berlindroid.zekompanion

import com.fazecast.jSerialComm.SerialPort


actual typealias Environment = Any

actual fun buildBadgeManager(environment: Environment): BadgeManager = object : JvmBadgeManager() {
    override fun getBadger2040s() = SerialPort.getCommPorts()
        .filter {
            // Device doesn't show any better descriptive name on Windows, this should be good enough
            it.portDescription.contains("CircuitPython")
        }
        .toList()
}
