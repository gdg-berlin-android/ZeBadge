package de.berlindroid.zeapp.ui

import android.app.Activity
import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.core.graphics.scale
import de.berlindroid.zeapp.PAGE_HEIGHT
import de.berlindroid.zeapp.PAGE_WIDTH
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.vm.BadgeViewModel

@Composable
fun PictureEditorDialog(
    activity: Activity,
    dismissed: () -> Unit = {},
    accepted: (config: BadgeViewModel.Configuration.Picture) -> Unit

) {
    var bitmap by remember {
        mutableStateOf(
            BitmapFactory.decodeResource(
                activity.resources,
                R.drawable.error,
            )
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        // success?
        bitmap = if (uri == null) {
            // nope, so show error bitmap
            Log.d("Picture", "Not found")
            BitmapFactory.decodeResource(
                activity.resources,
                R.drawable.error,
            )
        } else {
            // yes, so read image
            BitmapFactory.decodeStream(
                activity.contentResolver.openInputStream(uri)
            )
        }.scale(PAGE_WIDTH, PAGE_HEIGHT)
    }

    AlertDialog(
        onDismissRequest = dismissed,
        confirmButton = {
            Button(onClick = {
                accepted(BadgeViewModel.Configuration.Picture(bitmap))
            }) {
                Text(stringResource(id = android.R.string.ok))
            }
        },
        title = {
            Text("Set Picture")
        },
        text = {
            Column {
                BinaryImageEditor(bitmap = bitmap) {
                    bitmap = it
                }

                Button(onClick = {
                    launcher.launch(PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Text(text = stringResource(id = android.R.string.search_go))
                }
            }
        }
    )
}