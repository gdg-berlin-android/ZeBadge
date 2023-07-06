package de.berlindroid.zeapp

import android.app.Activity
import androidx.compose.runtime.staticCompositionLocalOf

internal val LocalZeActivity = staticCompositionLocalOf<Activity> {
    error("No LocalZeActivity provided")
}
