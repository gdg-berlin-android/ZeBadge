package de.berlindroid.zekompanion

import com.fazecast.jSerialComm.SerialPort


actual typealias Environment = Any

actual fun buildBadgeManager(environment: Environment): BadgeManager = object : JvmBadgeManager() {
    override fun getBadger2040s() = SerialPort.getCommPorts()
        .filter {
            println(it.systemPortName)
            true
            // return the first serial comm device, can't find it by descriptive name
//            it.descriptivePortName.contains("Badger")
        }
        .toList()
}
