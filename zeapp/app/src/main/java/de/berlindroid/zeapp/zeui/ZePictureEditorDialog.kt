package de.berlindroid.zeapp.zeui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import de.berlindroid.zeapp.PAGE_HEIGHT
import de.berlindroid.zeapp.PAGE_WIDTH
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.zebits.cropPageFromCenter
import de.berlindroid.zeapp.zebits.isBinary
import de.berlindroid.zeapp.zebits.scaleIfNeeded
import de.berlindroid.zeapp.zemodels.ZeConfiguration
import timber.log.Timber

private const val ROTATE_CLOCKWISE = 90f

/**
 * Editor Dialog for adding an image as a badge page.
 *
 * @param dismissed callback called when dismissed or cancelled
 * @param accepted callback called when user wants to take the selected image.
 */
@Composable
fun PictureEditorDialog(
    dismissed: () -> Unit = {},
    accepted: (config: ZeConfiguration.Picture) -> Unit,
    snackbarMessage: (String) -> Unit,
) {
    val context = LocalContext.current

    var bitmap by remember {
        mutableStateOf(
            BitmapFactory.decodeResource(
                context.resources,
                R.drawable.error,
            ).scaleIfNeeded(PAGE_WIDTH, PAGE_HEIGHT),
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        // success?
        bitmap = if (uri == null) {
            // nope, so show error bitmap
            Timber.d("Picture", "Not found")
            BitmapFactory.decodeResource(
                context.resources,
                R.drawable.error,
            ).scaleIfNeeded(PAGE_WIDTH, PAGE_HEIGHT)
        } else {
            // yes, so read image
            BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(uri),
            )
        }.cropPageFromCenter()
    }

    AlertDialog(
        onDismissRequest = dismissed,
        confirmButton = {
            Button(onClick = {
                if (bitmap.isBinary()) {
                    accepted(ZeConfiguration.Picture(bitmap))
                } else {
                    snackbarMessage(context.getString(R.string.not_binary_image))
                }
            },) {
                Text(stringResource(id = android.R.string.ok))
            }
        },
        title = {
            Text(stringResource(R.string.set_picture))
        },
        text = {
            Column {
                BinaryImageEditor(bitmap = bitmap) {
                    bitmap = it
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        launcher.launch(
                            PickVisualMediaRequest(
                                mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
                            ),
                        )
                    },
                ) {
                    Text(text = stringResource(id = android.R.string.search_go))
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        bitmap = rotateBitmap(bitmap)
                    },
                ) {
                    Text(text = stringResource(id = R.string.rotate))
                }
            }
        },
    )
}

private fun rotateBitmap(bitmap: Bitmap): Bitmap {
    val matrix = android.graphics.Matrix()
    matrix.postRotate(ROTATE_CLOCKWISE)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
