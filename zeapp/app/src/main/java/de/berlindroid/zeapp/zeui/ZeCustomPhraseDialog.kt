package de.berlindroid.zeapp.zeui

import android.R
import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import de.berlindroid.zeapp.LocalZeActivity
import de.berlindroid.zeapp.zebits.composableToBitmap
import de.berlindroid.zeapp.zebits.isBinary
import de.berlindroid.zeapp.zemodels.ZeConfiguration
import de.berlindroid.zeapp.zeui.zepages.CustomPhrasePage

@Composable
fun CustomPhraseEditorDialog(
    activity: Activity = LocalZeActivity.current,
    config: ZeConfiguration.CustomPhrase,
    dismissed: () -> Unit,
    accepted: (ZeConfiguration.CustomPhrase) -> Unit
) {
    var randomPhrase by remember { mutableStateOf(config.phrase) }
    var image by remember { mutableStateOf(config.bitmap) }

    fun redrawComposableImage() {
        composableToBitmap(
            activity = activity,
            content = { CustomPhrasePage(randomPhrase) },
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
                        accepted(ZeConfiguration.CustomPhrase(randomPhrase, image))
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
        title = { Text(text = "Add your phrase here") },
        properties = DialogProperties(),
        text = {
            Column {
                BinaryImageEditor(
                    bitmap = image,
                    bitmapUpdated = { image = it }
                )
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = randomPhrase,
                maxLines = 1,
                label = { Text(text = "Random Phrase") },
                onValueChange = { newValue ->
                    if (newValue.length <= MaxCharacters * 2) {
                        randomPhrase = newValue
                        redrawComposableImage()
                    }
                },
                supportingText = {
                    Text(text = "${randomPhrase.length}/${MaxCharacters * 2}")
                }
            )
        }
    )
}