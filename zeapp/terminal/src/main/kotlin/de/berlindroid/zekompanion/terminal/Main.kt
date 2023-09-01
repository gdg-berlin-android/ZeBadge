package de.berlindroid.zekompanion.terminal

import de.berlindroid.zekompanion.buildBadgeManager
import de.berlindroid.zekompanion.getPlatform

fun main() {
    println("Welcome ${getPlatform()}")

    val sneaky = if (buildBadgeManager("").isConnected()) "one" else "no"
    println("Currently the is $sneaky badge connected.")
}
