package de.berlindroid.zeapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.width
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.content.IntentCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import de.berlindroid.zeapp.zebits.toDitheredImage
import de.berlindroid.zeapp.zemodels.ZeConfiguration
import de.berlindroid.zeapp.zemodels.ZeSlot
import de.berlindroid.zeapp.zeui.zehome.ZeScreen
import de.berlindroid.zeapp.zeui.zehome.isSmartphoneSize
import de.berlindroid.zeapp.zeui.zehome.isTabletSize
import de.berlindroid.zeapp.zevm.ZeBadgeViewModel
import kotlinx.coroutines.launch
import android.content.res.Configuration as AndroidConfig
import androidx.compose.foundation.layout.Row as ZeRow
import androidx.compose.foundation.layout.Spacer as ZeSpacer
import androidx.compose.ui.Modifier as ZeModifier
import de.berlindroid.zeapp.zeui.simulator.BadgeSimulator as ZeSimulator

/**
 * Main View entrance for the app
 */
@AndroidEntryPoint
class ZeMainActivity : AppCompatActivity() {
    private val vm: ZeBadgeViewModel by viewModels()

    /**
     * Once created, use the main view composable.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (intent.type?.startsWith("image/") == true) {
            handleSendImage(intent)
        }
        setContent {
            DrawUi()
        }
    }

    private fun handleSendImage(intent: Intent) {
        IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
            ?.let(::updateSelectedImage)
    }

    private fun updateSelectedImage(imageUri: Uri) = lifecycleScope.launch {
        val bitmap = imageUri.toDitheredImage(this@ZeMainActivity)
        vm.slotConfigured(
            ZeSlot.Camera,
            ZeConfiguration.Camera(bitmap),
        )
    }

    @Composable
    private fun DrawUi() {
        val wsc = calculateWindowSizeClass(activity = this)

        if (!wsc.isTabletSize && wsc.isSmartphoneSize) {
            CompactUi()
        } else {
            LargeScreenUi(vm)
        }
    }

    @Composable
    private fun CompactUi() {
        if (LocalConfiguration.current.orientation == AndroidConfig.ORIENTATION_LANDSCAPE) {
            ZeSimulator(
                page = vm.slotToBitmap(vm.currentSimulatorSlot),
                onButtonPressed = vm::simulatorButtonPressed,
            )
        } else {
            ZeScreen(vm)
        }
    }

    @Composable
    private fun LargeScreenUi(vm: ZeBadgeViewModel) {
        ZeRow {
            ZeScreen(vm, modifier = Modifier.weight(.3f))
            ZeSpacer(modifier = ZeModifier.width(ZeDimen.Two))
            ZeSimulator(
                page = vm.slotToBitmap(vm.currentSimulatorSlot),
                onButtonPressed = vm::simulatorButtonPressed,
                modifier = Modifier.weight(.3f),
            )
        }
    }
}

const val ROUTE_HOME = "home"
const val ROUTE_ABOUT = "about"
const val ROUTE_OPENSOURCE = "opensource"
const val ROUTE_SETTINGS = "settings"
const val ROUTE_ZEPASS = "zepass"
const val ROUTE_LANGUAGES = "languages"
