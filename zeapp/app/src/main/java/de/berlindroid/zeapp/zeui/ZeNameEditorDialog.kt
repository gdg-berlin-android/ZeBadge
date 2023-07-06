@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package de.berlindroid.zeapp.zeui

import android.R
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import de.berlindroid.zeapp.LocalActivity
import de.berlindroid.zeapp.zebits.composableToBitmap
import de.berlindroid.zeapp.zebits.isBinary
import de.berlindroid.zeapp.zemodels.ZeConfiguration
import de.berlindroid.zeapp.zeui.zepages.NamePage


/**
 * Editor dialog for changing the name of the participant badge.
 *
 * @param config configuration of the slot, containing details to be displayed
 * @param dismissed callback called when dialog is dismissed / cancelled
 * @param accepted callback called with the new configuration configured.
 */

const val MaxCharacters: Int = 16
private const val Empty = ""

@Composable
fun NameEditorDialog(
    config: ZeConfiguration.Name,
    dismissed: () -> Unit = {},
    accepted: (config: ZeConfiguration.Name) -> Unit
) {
    var name by remember { mutableStateOf(config.name) }
    var contact by remember { mutableStateOf(config.contact) }
    var image by remember { mutableStateOf(config.bitmap) }
    val activity = LocalActivity.current

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
                        accepted(ZeConfiguration.Name(name, contact, image))
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
        dismissButton = {
            Button(onClick = dismissed) {
                Text(text = "Cancel")
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
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = name,
                        maxLines = 1,
                        label = { Text(text = "Name") },
                        onValueChange = { newValue ->
                            if (newValue.length <= MaxCharacters * 2) {
                                name = newValue
                                redrawComposableImage()
                            }
                        },
                        supportingText = {
                            Text(text = "${name.length}/${MaxCharacters * 2}")
                        },
                        trailingIcon = {
                            ClearIcon(isEmpty = name.isEmpty()) {
                                name = Empty
                            }
                        }
                    )
                }

                item {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = contact,
                        maxLines = 1,
                        label = { Text(text = "Contact") },
                        onValueChange = { newValue ->
                            // Limit Characters so they're displayed correctly in the screen
                            if (newValue.length <= MaxCharacters) {
                                contact = newValue
                                redrawComposableImage()
                            }
                        },
                        supportingText = {
                            Text(text = "${contact.length}/$MaxCharacters")
                        },
                        trailingIcon = {
                            ClearIcon(isEmpty = contact.isEmpty()) {
                                contact = Empty
                            }
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun ClearIcon(isEmpty: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    if (!isEmpty) {
        Icon(
            Icons.Rounded.Clear,
            contentDescription = "Clear",
            modifier = modifier.clickable {
                onClick()
            }
        )
    }
}
