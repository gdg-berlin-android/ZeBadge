package de.berlindroid.zeapp.zeentrypoint

import android.content.res.Configuration
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import de.berlindroid.zeapp.ZeDimen
import de.berlindroid.zeapp.zeui.simulator.BadgeSimulator
import de.berlindroid.zeapp.zeui.zehome.ZeScreen
import de.berlindroid.zeapp.zeui.zehome.isSmartphoneSize
import de.berlindroid.zeapp.zeui.zehome.isTabletSize

fun ZeMainActivity.drawsUi() {
    setContent {
        DrawUi()
    }
}

@Composable
private fun ZeMainActivity.DrawUi() {
    val wsc = calculateWindowSizeClass(activity = this)

    if (!wsc.isTabletSize && wsc.isSmartphoneSize) {
        CompactUi()
    } else {
        LargeScreenUi()
    }
}

@Composable
private fun ZeMainActivity.CompactUi() {
    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        BadgeSimulator(
            page = vm.slotToBitmap(vm.currentSimulatorSlot),
            onButtonPressed = vm::simulatorButtonPressed,
        )
    } else {
        ZeScreen(vm)
    }
}

@Composable
private fun ZeMainActivity.LargeScreenUi() {
    Row {
        ZeScreen(vm, modifier = Modifier.weight(.3f))
        Spacer(modifier = Modifier.width(ZeDimen.Two))
        BadgeSimulator(
            page = vm.slotToBitmap(vm.currentSimulatorSlot),
            onButtonPressed = vm::simulatorButtonPressed,
            modifier = Modifier.weight(.3f),
        )
    }
}
