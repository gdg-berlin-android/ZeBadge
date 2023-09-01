package de.berlindroid.zekompanion
class ApplePlatform : Platform {
    override val name: String = "Apple!!!"
}
actual fun getPlatform(): Platform = ApplePlatform()
