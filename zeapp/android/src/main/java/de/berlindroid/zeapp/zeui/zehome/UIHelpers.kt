package de.berlindroid.zeapp.zeui.zehome

import android.view.ViewTreeObserver
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import de.berlindroid.zeapp.zemodels.ZeSlot

internal val ZeSlot.isSponsor: Boolean
    get() = this is ZeSlot.FirstSponsor

// Device size extensions
internal val WindowSizeClass.isTabletSize: Boolean
    get() =
        this.widthSizeClass == WindowWidthSizeClass.Expanded &&
            (
                this.heightSizeClass == WindowHeightSizeClass.Expanded ||
                    this.heightSizeClass == WindowHeightSizeClass.Medium
            )

internal val WindowSizeClass.isSmartphoneSize: Boolean
    get() =
        this.widthSizeClass in WindowWidthSizeClass.DefaultSizeClasses &&
            (this.heightSizeClass == WindowHeightSizeClass.Compact)

@Composable
fun isKeyboardVisibleState(): State<Boolean> {
    val view = LocalView.current
    var isImeVisible by remember { mutableStateOf(false) }

    DisposableEffect(LocalWindowInfo.current) {
        val listener =
            ViewTreeObserver.OnPreDrawListener {
                isImeVisible = ViewCompat
                    .getRootWindowInsets(view)
                    ?.isVisible(WindowInsetsCompat.Type.ime()) == true
                true
            }
        view.viewTreeObserver.addOnPreDrawListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnPreDrawListener(listener)
        }
    }
    return rememberUpdatedState(isImeVisible)
}
