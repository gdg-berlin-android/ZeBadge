package de.berlindroid.zekompanion.desktop.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap

@Composable
fun ImageEditor(image: ImageBitmap, sendToBadge: () -> Unit) {
    Column {
        Row {
            Spacer(modifier = Modifier.weight(1.0f))
            Image(
                modifier = Modifier.weight(1.0f),
                bitmap = image,
                contentDescription = null,
            )

            Spacer(modifier = Modifier.weight(1.0f))
        }

        Spacer(modifier = Modifier.weight(1.0f))

        ImageManipulators {
            sendToBadge()
        }
    }
}

