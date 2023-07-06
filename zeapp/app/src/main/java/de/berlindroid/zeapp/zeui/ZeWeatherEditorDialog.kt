@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package de.berlindroid.zeapp.zeui

import android.R
import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import de.berlindroid.zeapp.zebits.composableToBitmap
import de.berlindroid.zeapp.zebits.isBinary
import de.berlindroid.zeapp.zeui.zepages.WeatherPage
import de.berlindroid.zeapp.zevm.ZeBadgeViewModel.Configuration


/**
 * Editor dialog for selecting the weather
 *
 * @param activity Android activity to be used for rendering the composable.
 * @param config configuration of the slot, containing details to be displayed
 * @param dismissed callback called when dialog is dismissed / cancelled
 * @param accepted callback called with the new configuration configured.
 */

private const val Empty = ""

@Composable
fun WeatherEditorDialog(
    activity: Activity,
    config: Configuration.Weather,
    dismissed: () -> Unit = {},
    accepted: (config: Configuration.Weather) -> Unit
) {
    var date by remember { mutableStateOf(config.date) }
    var temperature by remember { mutableStateOf(config.temperature) }
    var image by remember { mutableStateOf(config.bitmap) }

    fun redrawComposableImage() {
        composableToBitmap(
            activity = activity,
            content = {
                WeatherPage(date, temperature)
                      },
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
                        accepted(Configuration.Weather(date, temperature, image))
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
                        value = date,
                        maxLines = 1,
                        label = { Text(text = "Date") },
                        onValueChange = { newValue ->
                            if (newValue.length <= MaxCharacters * 2) {
                                date = newValue
                                redrawComposableImage()
                            }
                        },
                        supportingText = {
                            Text(text = "${date.length}/${MaxCharacters * 2}")
                        },
                        trailingIcon = {
                            ClearIcon(isEmpty = date.isEmpty()) {
                                date = Empty
                            }
                        }
                    )
                }

                item {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = temperature,
                        maxLines = 1,
                        label = { Text(text = "Temperature") },
                        onValueChange = { newValue ->
                            // Limit Characters so they're displayed correctly in the screen
                            if (newValue.length <= MaxCharacters) {
                                temperature = newValue
                                redrawComposableImage()
                            }
                        },
                        supportingText = {
                            Text(text = "${temperature.length}/$MaxCharacters")
                        },
                        trailingIcon = {
                            ClearIcon(isEmpty = temperature.isEmpty()) {
                                temperature = Empty
                            }
                        }
                    )
                }
            }
        }
    )
}