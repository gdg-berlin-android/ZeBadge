@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package de.berlindroid.zeapp.zeui

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import com.ban.autosizetextfield.AutoSizeTextField
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.zebits.composableToBitmap
import de.berlindroid.zeapp.zebits.isBinary
import de.berlindroid.zeapp.zemodels.ZeConfiguration
import de.berlindroid.zeapp.zeui.zepages.NamePage
import de.berlindroid.zeapp.zeui.zetheme.ZeBlack
import de.berlindroid.zeapp.zeui.zetheme.ZeWhite

const val MaxCharacters: Int = 20

/**
 * Editor dialog for changing the name of the participant badge.
 *
 * @param config configuration of the slot, containing details to be displayed
 * @param dismissed callback called when dialog is dismissed / cancelled
 * @param accepted callback called with the new configuration configured.
 * @param updateMessage callback to display a message
 */
@Composable
fun NameEditorDialog(
    config: ZeConfiguration.Name,
    dismissed: () -> Unit = {},
    accepted: (config: ZeConfiguration.Name) -> Unit,
    updateMessage: (String) -> Unit,
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
        containerColor = ZeWhite,
        confirmButton = {
            Button(
                onClick = {
                    if (image.isBinary()) {
                        accepted(ZeConfiguration.Name(name, contact, image))
                    } else {
                        updateMessage(activity.resources.getString(R.string.binary_image_needed))
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
        title = {
            Text(
                color = ZeBlack,
                text = stringResource(R.string.add_your_contact_details),
            )
        },
        properties = DialogProperties(decorFitsSystemWindows = false),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                BinaryImageEditor(
                    bitmap = image,
                    bitmapUpdated = { image = it },
                )

                AutoSizeTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = name ?: "",
                    onValueChange = { newValue ->
                        if (newValue.length <= MaxCharacters * 2) {
                            name = newValue
                            redrawComposableImage()
                        }
                    },
                    supportingText = {
                        Text(text = "${name?.length ?: 0}/${MaxCharacters * 2}")
                    },
                    trailingIcon = {
                        ClearIcon(isEmpty = name?.isEmpty() ?: true) {
                            name = ""
                        }
                    },
                    placeholder = { Text(text = stringResource(R.string.name)) },
                )

                AutoSizeTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = contact ?: "",
                    placeholder = { Text(text = stringResource(R.string.contact)) },
                    onValueChange = { newValue ->
                        // Limit Characters so they're displayed correctly in the screen
                        if (newValue.length <= MaxCharacters) {
                            contact = newValue
                            redrawComposableImage()
                        }
                    },
                    supportingText = {
                        Text(text = "${contact?.length ?: 0}/$MaxCharacters")
                    },
                    trailingIcon = {
                        ClearIcon(isEmpty = contact?.isEmpty() ?: true) {
                            contact = ""
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
            modifier = modifier.clickable(onClick = onClick),
        )
    }
}
