@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package de.berlindroid.zeapp.zeui

import android.R
import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import de.berlindroid.zeapp.zebits.composableToBitmap
import de.berlindroid.zeapp.zebits.isBinary
import de.berlindroid.zeapp.zemodels.ZeConfiguration
import de.berlindroid.zeapp.zeui.zepages.RandomQuotePage
import kotlin.random.Random


/**
 * Editor dialog for selecting the quote of the day
 *
 * @param activity Android activity to be used for rendering the composable.
 * @param config configuration of the slot, containing details to be displayed
 * @param dismissed callback called when dialog is dismissed / cancelled
 * @param accepted callback called with the new configuration configured.
 */

@Composable
fun RandomQuotesEditorDialog(
    activity: Activity,
    config: ZeConfiguration.Quote,
    dismissed: () -> Unit = {},
    accepted: (config: ZeConfiguration.Quote) -> Unit
) {
    var message by remember { mutableStateOf(config.message) }
    var author by remember { mutableStateOf(config.author) }
    var image by remember { mutableStateOf(config.bitmap) }



    fun redrawComposableImage() {
        composableToBitmap(
            activity = activity,
            content = {
                RandomQuotePage(message, author)
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
                        accepted(ZeConfiguration.Quote(message, author, image))
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
        title = { Text(text = "Click Get to show quote of the day") },
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
                    Button(onClick = {
                        val index = Random.nextInt(0, 10)
                        message = quoteList[index].q
                        author = quoteList[index].a
                        redrawComposableImage()
                    }) {
                        Text(text = "Get")
                    }
                }
            }
        }
    )
}

data class Quote(
    val q: String,
    val a: String,
)

private val quoteList = listOf<Quote>(
    Quote("You win more from losing than winning.","Morgan Wootten"),
    Quote("The Only Thing That Is Constant Is Change","Heraclitus"),
    Quote("Once you choose hope, anything's possible.","Christopher Reeve"),
    Quote("The love of money is the root of all evil.","the Bible"),
    Quote("The only thing we have to fear is fear itself.","Franklin D. Roosevelt"),
    Quote("Whoever is happy will make others happy too.","Anne Frank"),
    Quote("The purpose of our lives is to be happy","Dalai Lama"),
    Quote("Only a life lived for others is a life worthwhile.","Albert Einstein"),
    Quote("Live in the sunshine, swim the sea, drink the wild air.","Ralph Waldo Emerson"),
    Quote("Life is trying things to see if they work.","Ralph Waldo Emerson"),
)
