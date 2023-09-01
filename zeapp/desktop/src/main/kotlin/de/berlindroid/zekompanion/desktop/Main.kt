package de.berlindroid.zekompanion.desktop

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import de.berlindroid.zekompanion.buildBadgeManager
import de.berlindroid.zekompanion.getPlatform

@Composable
@Preview
fun App() {
    val platform = getPlatform()
    val manager = buildBadgeManager("")

    MaterialTheme {
        Column {
            Text("Hello $platform.")
            Text("Currently there is")
            if (manager.isConnected()) {
                Text(" one ")
            } else {
                Text(" no ")
            }
            Text("badge connected.")
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
