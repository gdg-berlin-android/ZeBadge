package de.berlindroid.zeapp

import android.app.Activity
import androidx.compose.runtime.staticCompositionLocalOf

internal val LocalActivity = staticCompositionLocalOf<Activity> {
    error("No LocalActivity provided")
}
