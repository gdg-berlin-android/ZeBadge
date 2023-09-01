package de.berlindroid.zekompanion

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform