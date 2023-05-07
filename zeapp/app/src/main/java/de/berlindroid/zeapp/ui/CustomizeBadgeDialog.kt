@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package de.berlindroid.zeapp.ui

import android.R
import android.app.Activity
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.window.DialogProperties
import de.berlindroid.zeapp.ui.pages.NamePage
import de.berlindroid.zeapp.bits.composableToBitmap
import de.berlindroid.zeapp.bits.isBinary

@Composable
fun CustomizeBadgeDialog(
    activity: Activity,
    initialBadge: Bitmap,
    initialName: String,
    initialContact: String,
    dismissed: () -> Unit = {},
    accepted: (badge: Bitmap, name: String, contact: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var contact by remember { mutableStateOf(initialContact) }
    var image by remember { mutableStateOf(initialBadge) }

    fun redrawComposableImage() {
        composableToBitmap(
            activity = activity,
            content = { NamePage(name, contact) },
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
                        accepted(image, name, contact)
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
        title = { Text(text = "Add your name") },
        properties = DialogProperties(),
        text = {
            LazyColumn {
                item {
                    BinaryImageEditor(
                        bitmap = image,
                        refresh = { image = initialBadge },
                        bitmapUpdated = { image = it }
                    )
                }

                item {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = name,
                        label = { Text(text = "Name") },
                        onValueChange = { newValue ->
                            name = newValue
                            redrawComposableImage()
                        }
                    )
                }

                item {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = contact,
                        label = { Text(text = "Contact") },
                        onValueChange = { newValue ->
                            contact = newValue
                            redrawComposableImage()
                        }
                    )
                }
            }
        }
    )
}