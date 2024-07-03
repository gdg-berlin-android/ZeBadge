package de.berlindroid.zeapp.zeui.zetheme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ZeWhite,
    secondary = ZeBlack,
    onPrimary = ZeBlack,
    onSecondary = ZeWhite,
    error = ZeWhite,
    onError = ZeBlack,
    background = ZeWhite,
    onBackground = ZeBlack,
)

private val LightColorScheme = lightColorScheme(
    primary = ZeTeal,
    secondary = ZePurple,
    onPrimary = ZeWhite,
    onSecondary = ZeBlack,
    surface = ZeGrey,
    onSurface = ZeBlack,
    background = ZeWhite,
    error = ZeCarmine,
    onError = ZeWhite,
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
