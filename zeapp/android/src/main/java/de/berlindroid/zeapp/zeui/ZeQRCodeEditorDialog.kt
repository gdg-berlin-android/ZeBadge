@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package de.berlindroid.zeapp.zeui

import android.app.Activity
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.ZeDimen
import de.berlindroid.zeapp.zebits.ZeVcardStringBuilder
import de.berlindroid.zeapp.zebits.isBinary
import de.berlindroid.zeapp.zebits.qrComposableToBitmap
import de.berlindroid.zeapp.zemodels.ZeConfiguration

/**
 * Editor dialog for changing the name of the participant badge.
 *
 * @param config configuration of the slot, containing details to be displayed
 * @param dismissed callback called when dialog is dismissed / cancelled
 * @param accepted callback called with the new configuration configured.
 * @param snackbarMessage callback to display a snackbar message
 */
@Composable
fun QRCodeEditorDialog(
    config: ZeConfiguration.QRCode,
    dismissed: () -> Unit = {},
    accepted: (config: ZeConfiguration.QRCode) -> Unit,
    snackbarMessage: (String) -> Unit,
) {
    val activity = LocalContext.current as Activity

    var title by remember { mutableStateOf(config.title) }
    var text by remember { mutableStateOf(config.text) }
    var url by remember { mutableStateOf(config.url) }
    var image by remember { mutableStateOf(config.bitmap) }
    var isVcard by remember { mutableStateOf(config.isVcard) }
    var phone by remember { mutableStateOf(config.phone) }
    var email by remember { mutableStateOf(config.email) }

    val vCardString by remember {
        derivedStateOf {
            ZeVcardStringBuilder(
                formattedName = text,
                title = title,
                phone = phone,
                email = email,
                url = url,
            ).buildString()
        }
    }

    fun redrawComposableImage() {
        qrComposableToBitmap(
            activity = activity,
            title = title,
            text = text,
            qrContent = if (isVcard) vCardString else url,
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
                        accepted(
                            ZeConfiguration.QRCode(
                                title = title,
                                text = text,
                                url = url,
                                isVcard = isVcard,
                                phone = phone,
                                email = email,
                                bitmap = image,
                            ),
                        )
                    } else {
                        snackbarMessage(activity.getString(R.string.image_needed))
                    }
                },
            ) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
        title = { Text(text = stringResource(id = R.string.add_qr_url)) },
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = stringResource(id = R.string.qr_vcard))
                        Spacer(modifier = Modifier.width(ZeDimen.One))
                        Switch(checked = isVcard, onCheckedChange = {
                            isVcard = it
                            redrawComposableImage()
                        },)
                    }
                }

                basicFields(
                    redrawComposableImage = ::redrawComposableImage,
                    title = title,
                    onTitleChange = { title = it },
                    text = text,
                    onTextChange = { text = it },
                    url = url,
                    onUrlChange = { url = it },
                )

                if (isVcard) {
                    additionalVcardFields(
                        redrawComposableImage = ::redrawComposableImage,
                        email = email,
                        onEmailChange = { email = it },
                        phone = phone,
                        onPhoneChange = { phone = it },
                    )
                }
            }
        },
    )
}

fun LazyListScope.basicFields(
    redrawComposableImage: () -> Unit,
    title: String,
    onTitleChange: (String) -> Unit,
    text: String,
    onTextChange: (String) -> Unit,
    url: String,
    onUrlChange: (String) -> Unit,
) {
    item {
        RedrawTextField(
            value = title,
            textId = R.string.qr_code_title,
            onValueChange = onTitleChange,
            redrawComposableImage = redrawComposableImage,
        )
    }

    item {
        RedrawTextField(
            value = text,
            textId = R.string.qr_code_text,
            onValueChange = onTextChange,
            redrawComposableImage = redrawComposableImage,
        )
    }

    item {
        RedrawTextField(
            value = url,
            textId = R.string.url,
            onValueChange = onUrlChange,
            redrawComposableImage = redrawComposableImage,
        )
    }
}
fun LazyListScope.additionalVcardFields(
    redrawComposableImage: () -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
) {
    item {
        RedrawTextField(
            value = email,
            textId = R.string.qr_code_email,
            onValueChange = onEmailChange,
            redrawComposableImage = redrawComposableImage,
        )
    }
    item {
        RedrawTextField(
            value = phone,
            textId = R.string.qr_code_phone,
            onValueChange = onPhoneChange,
            redrawComposableImage = redrawComposableImage,
        )
    }
}

@Composable
fun RedrawTextField(
    value: String,
    @StringRes textId: Int,
    onValueChange: (String) -> Unit,
    redrawComposableImage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        maxLines = 1,
        singleLine = true,
        label = { Text(text = stringResource(id = textId)) },
        onValueChange = { newValue ->
            onValueChange(newValue)
            redrawComposableImage()
        },
    )
}
