package de.berlindroid.zeapp.ui

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
import de.berlindroid.zeapp.bits.composableToBitmap
import de.berlindroid.zeapp.bits.isBinary
import de.berlindroid.zeapp.ui.pages.NamePage
import de.berlindroid.zeapp.ui.pages.RandomPhrasePage
import de.berlindroid.zeapp.vm.BadgeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomPhraseEditorDialog(
    activity: Activity,
    config: BadgeViewModel.Configuration.RandomPhrase,
    dismissed: () -> Unit,
    accepted: (BadgeViewModel.Configuration.RandomPhrase) -> Unit
) {
    var randomPhrase by remember { mutableStateOf(config.phrase) }
    var image by remember { mutableStateOf(config.bitmap) }

    fun redrawComposableImage() {
        composableToBitmap(
            activity = activity,
            content = { RandomPhrasePage(randomPhrase) },
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
                        accepted(BadgeViewModel.Configuration.RandomPhrase(randomPhrase, image))
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