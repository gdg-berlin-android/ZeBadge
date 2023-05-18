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
import de.berlindroid.zeapp.bits.composableToBitmap
import de.berlindroid.zeapp.bits.isBinary
import de.berlindroid.zeapp.ui.pages.NamePage
import de.berlindroid.zeapp.vm.BadgeViewModel.Configuration

/**
 * Editor dialog for changing the name of the participant badge.
 *
 * @param activity Android activity to be used for rendering the composable.
 * @param config configuration of the slot, containing details to be displayed
 * @param dismissed callback called when dialog is dismissed / cancelled
 * @param accepted callback called with the new configuration configured.
 */
@Composable
fun NameEditorDialog(
    activity: Activity,
    config: Configuration.Name,
    dismissed: () -> Unit = {},
    accepted: (config: Configuration.Name) -> Unit
) {
    var name by remember { mutableStateOf(config.name) }
    var contact by remember { mutableStateOf(config.contact) }
    var image by remember { mutableStateOf(config.bitmap) }

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
                        accepted(Configuration.Name(name, contact, image))
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
        title = { Text(text = "Add your contact details") },
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
                        value = name,
                        maxLines = 1,
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
                        maxLines = 1,
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