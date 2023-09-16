package de.berlindroid.zekompanion

actual fun getPlatform(): String = "Android ${android.os.Build.VERSION.SDK_INT}"
