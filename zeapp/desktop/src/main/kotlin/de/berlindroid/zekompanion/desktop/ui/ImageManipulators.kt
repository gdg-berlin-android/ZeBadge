package de.berlindroid.zekompanion.desktop.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ImageManipulators(sendToBadge: () -> Unit) {
    // TODO: Add negate, fs vs threshold, rotation manipulators before the spacer 👩‍🚀
    Row {
        Spacer(modifier = Modifier.weight(1.0f))
        Button(
            content = {
                Text("send to badge")
            },
            onClick = sendToBadge,
        )
    }
}
