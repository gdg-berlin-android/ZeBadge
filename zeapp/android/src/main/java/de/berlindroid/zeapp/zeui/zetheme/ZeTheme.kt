package de.berlindroid.zeapp.zeui.zetheme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ZeWhite,
    secondary = ZeGrey,
    onPrimary = ZeBlack,
    onSecondary = ZeBlack,
    error = ZeCarmine,
    onError = ZeBlack,
    background = ZeBlack,
    onBackground = ZeWhite,
)

private val LightColorScheme = lightColorScheme(
    primary = ZeBlack,
    secondary = ZeGrey,
    onPrimary = ZeWhite,
    onSecondary = ZeBlack,
    error = ZeCarmine,
    onError = ZeWhite,
    background = ZeWhite,
    onBackground = ZeBlack,
)

@Composable
fun ZeBadgeAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
