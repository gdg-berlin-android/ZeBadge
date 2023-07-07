@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package de.berlindroid.zeapp.zeui

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import de.berlindroid.zeapp.R
import com.ban.autosizetextfield.AutoSizeTextField
import de.berlindroid.zeapp.zebits.composableToBitmap
import de.berlindroid.zeapp.zebits.isBinary
import de.berlindroid.zeapp.zemodels.ZeConfiguration
import de.berlindroid.zeapp.zeui.zepages.NamePage

const val MaxCharacters: Int = 20
private const val Empty = ""

/**
 * Editor dialog for changing the name of the participant badge.
 *
 * @param config configuration of the slot, containing details to be displayed
 * @param dismissed callback called when dialog is dismissed / cancelled
 * @param accepted callback called with the new configuration configured.
 * @param snackbarMessage callback to display a snackbar message
 */
@Composable
fun NameEditorDialog(
    config: ZeConfiguration.Name,
    dismissed: () -> Unit = {},
    accepted: (config: ZeConfiguration.Name) -> Unit,
    snackbarMessage: (String) -> Unit,
) {
    val activity = LocalContext.current as Activity

    var name by remember { mutableStateOf(config.name) }
    var contact by remember { mutableStateOf(config.contact) }
    var image by remember { mutableStateOf(config.bitmap) }

    fun redrawComposableImage() {
        composableToBitmap(
            activity = activity,
            content = { NamePage(name ?: "", contact ?: "") },
        ) {
            image = it
        }
    }

    AlertDialog(
        modifier = Modifier.imePadding(),
        onDismissRequest = dismissed,
        confirmButton = {
            Button(
                onClick = {
                    if (image.isBinary()) {
                        accepted(ZeConfiguration.Name(name, contact, image))
                    } else {
                        snackbarMessage(activity.resources.getString(R.string.binary_image_needed))
                    }
                },
            ) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            Button(onClick = dismissed) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
        title = { Text(text = stringResource(R.string.add_your_contact_details)) },
        properties = DialogProperties(decorFitsSystemWindows = false),
        text = {
            Column {
                BinaryImageEditor(
                    bitmap = image,
                    bitmapUpdated = { image = it },
                )

                AutoSizeTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = name ?: "",
                    placeholder = { Text(text = stringResource(R.string.name)) },
                    onValueChange = { newValue ->
                        if (newValue.length <= MaxCharacters * 2) {
                            name = newValue
                            redrawComposableImage()
                        }
                    },
                    supportingText = {
                        Text(text = "${name?.length}/${MaxCharacters * 2}")
                    },
                    trailingIcon = {
                        ClearIcon(isEmpty = name?.isEmpty() ?: true) {
                            name = Empty
                        }
                    },
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = contact ?: "",
                    maxLines = 1,
                    singleLine = true,
                    placeholder = { Text(text = stringResource(R.string.contact)) },
                    onValueChange = { newValue ->
                        // Limit Characters so they're displayed correctly in the screen
                        if (newValue.length <= MaxCharacters) {
                            contact = newValue
                            redrawComposableImage()
                        }
                    },
                    supportingText = {
                        Text(text = "${contact?.length}/$MaxCharacters")
                    },
                    trailingIcon = {
                        ClearIcon(isEmpty = contact?.isEmpty() ?: true) {
                            contact = Empty
                        }
                    },
                )
            }
        },
    )
}

@Composable
fun ClearIcon(isEmpty: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    if (!isEmpty) {
        Icon(
            Icons.Rounded.Clear,
            contentDescription = stringResource(R.string.clear),
            modifier = modifier.clickable {
                onClick()
            },
        )
    }
}
