@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package de.berlindroid.zeapp.ui

import android.R
import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import de.berlindroid.zeapp.LocalActivity
import de.berlindroid.zeapp.bits.isBinary
import de.berlindroid.zeapp.bits.qrComposableToBitmap
import de.berlindroid.zeapp.vm.ZeBadgeViewModel.Configuration

/**
 * Editor dialog for changing the name of the participant badge.
 *
 * @param config configuration of the slot, containing details to be displayed
 * @param dismissed callback called when dialog is dismissed / cancelled
 * @param accepted callback called with the new configuration configured.
 */
@Composable
fun QRCodeEditorDialog(
    config: Configuration.QRCode,
    dismissed: () -> Unit = {},
    accepted: (config: Configuration.QRCode) -> Unit
) {
    var title by remember { mutableStateOf(config.title) }
    var url by remember { mutableStateOf(config.url) }
    var image by remember { mutableStateOf(config.bitmap) }
    val activity = LocalActivity.current

    fun redrawComposableImage() {
        qrComposableToBitmap(
            activity = activity,
            title = title,
            url = url,
        ) {
            image = it
        }
    }

    AlertDialog(
        onDismissRequest = dismissed,
        confirmButton = {
            Button(
                onClick = {
                    if (image.isBinary()) {
                        accepted(Configuration.QRCode(title, url, image))
                    } else {
                        Toast.makeText(
                            activity,
                            "Binary image needed. Press one of the buttons below the image.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }) {
                Text(text = stringResource(id = R.string.ok))
            }
        },
        title = { Text(text = "Add your QR url") },
        properties = DialogProperties(),
        text = {
            LazyColumn {
                item {
                    BinaryImageEditor(
                        bitmap = image,
                        bitmapUpdated = { image = it }
                    )
                }

                item {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = title,
                        maxLines = 1,
                        label = { Text(text = "QR Code title") },
                        onValueChange = { newValue ->
                            title = newValue
                            redrawComposableImage()
                        }
                    )
                }

                item {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = url,
                        maxLines = 1,
                        label = { Text(text = "URL") },
                        onValueChange = { newValue ->
                            url = newValue
                            redrawComposableImage()
                        }
                    )
                }
            }
        }
    )
}
