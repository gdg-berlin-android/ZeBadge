@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package de.berlindroid.zeapp.zeui

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import de.berlindroid.zeapp.zebits.composableToBitmap
import de.berlindroid.zeapp.zebits.isBinary
import de.berlindroid.zeapp.zemodels.ZeConfiguration
import de.berlindroid.zeapp.zeservices.WeatherData
import de.berlindroid.zeapp.zeservices.fetchWeather
import de.berlindroid.zeapp.zeui.zepages.WeatherPage
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Editor dialog for selecting the weather
 *
 * @param config configuration of the slot, containing details to be displayed
 * @param dismissed callback called when dialog is dismissed / cancelled
 * @param accepted callback called with the new configuration configured.
 */
@Suppress("LongMethod")
@Composable
fun WeatherEditorDialog(
    config: ZeConfiguration.Weather,
    dismissed: () -> Unit = {},
    accepted: (config: ZeConfiguration.Weather) -> Unit,
    snackbarMessage: (String) -> Unit,
) {
    val activity = LocalContext.current as Activity

    var date by remember { mutableStateOf(config.date) }
    val temperature by remember { mutableStateOf(config.temperature) }
    var image by remember { mutableStateOf(config.bitmap) }

    var weatherData: WeatherData? by remember {
        mutableStateOf(null)
    }

    val scope = rememberCoroutineScope()

    fun redrawComposableImage() {
        composableToBitmap(
            activity = activity,
            content = {
                WeatherPage(
                    date = weatherData?.formattedDate() ?: "N/A",
                    temperature = weatherData?.formattedTemperature ?: "N/A",
                )
            },
            callback = { image = it },
        )
    }

    AlertDialog(
        onDismissRequest = dismissed,
        confirmButton = {
            Button(
                onClick = {
                    if (image.isBinary()) {
                        accepted(ZeConfiguration.Weather(date, temperature, image))
                    } else {
                        snackbarMessage("Binary image needed. Press one of the buttons below the image.")
                    }
                },
            ) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            Button(onClick = dismissed) {
                Text(text = stringResource(android.R.string.cancel))
            }
        },
        title = { Text(text = "Add your contact details") },
        properties = DialogProperties(),
        text = {
            LazyColumn {
                item {
                    BinaryImageEditor(
                        bitmap = image,
                        bitmapUpdated = { image = it },
                    )
                }

                item {
                    var openDialog by remember { mutableStateOf(false) }

                    if (openDialog) {
                        val datePickerState = rememberDatePickerState(
                            initialSelectedDateMillis = Instant.now().toEpochMilli(),
                        )
                        val confirmEnabled by remember {
                            derivedStateOf { datePickerState.selectedDateMillis != null }
                        }
                        DatePickerDialog(
                            onDismissRequest = {
                                openDialog = false
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        openDialog = false
                                        date = datePickerState.selectedDateMillis?.let {
                                            Instant.ofEpochMilli(it).atOffset(ZoneOffset.UTC)
                                        }?.format(DateTimeFormatter.ISO_LOCAL_DATE).toString()
                                    },
                                    enabled = confirmEnabled,
                                ) {
                                    Text(stringResource(android.R.string.ok))
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        openDialog = false
                                    },
                                ) {
                                    Text(stringResource(android.R.string.cancel))
                                }
                            },
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                    ) {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .focusable(false),
                            interactionSource = remember { MutableInteractionSource() },
                            readOnly = true,
                            value = date,
                            maxLines = 1,
                            label = { Text(text = "Date") },
                            onValueChange = { newValue ->
                                if (newValue.length <= MaxCharacters * 2) {
                                    date = newValue
                                    redrawComposableImage()
                                }
                            },
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable {
                                    if (!openDialog) {
                                        openDialog = true
                                    }
                                },
                        )
                    }
                }

                item {
                    Button(
                        onClick = {
                            // Fix this please :)
                            scope.launch {
                                weatherData = fetchWeather(date)
                                redrawComposableImage()
                            }
                        },
                    ) {
                        Text("Load Weather")
                    }
                }
            }
        },
    )
}
