package de.berlindroid.zekompanion

import com.fazecast.jSerialComm.SerialPort


actual typealias Environment = Any

actual fun buildBadgeManager(environment: Environment): BadgeManager = object : JvmBadgeManager() {
    override fun getBadger2040s() = SerialPort.getCommPorts().filter {
        it.descriptivePortName.contains("Badger") &&
                !it.systemPortPath.contains("tty") &&
                it.systemPortPath.contains("cu")
    }.toList()
}
