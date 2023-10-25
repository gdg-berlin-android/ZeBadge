package de.berlindroid.zekompanion.desktop.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.berlindroid.zekompanion.desktop.State
import de.berlindroid.zekompanion.desktop.State.EditImage
import de.berlindroid.zekompanion.desktop.State.EditNameBadge
import de.berlindroid.zekompanion.desktop.State.Undecided
import de.berlindroid.zekompanion.getPlatform
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.imageio.ImageIO

@Composable
@Preview
fun ZeDesktopApp(sendToBadge: (State) -> Unit) {
    val platform = getPlatform()

    var state: State by remember { mutableStateOf(Undecided) }

    Column {
        Header(state) { state = it }

        Content(
            state = state,
            sendToBadge = { sendToBadge(state) },
            loadConfig = { fileName -> File(fileName).readText().fromJSONtoConfig() },
            saveConfig = { config, fileName -> File(fileName).writeText(config.toJSON()) },
            stateUpdated = { state = it },
        )

        Footer(platform)
    }
}

@Composable
private fun Header(state: State, updateState: (State) -> Unit) {
    val goBack = @Composable {
        Button(
            onClick = {
                updateState(Undecided)
            },
        ) {
            Text("HOME")
        }
    }

    when (state) {
        is EditImage -> goBack()
        is EditNameBadge -> goBack()
        else -> Unit
    }
}

@Composable
fun ColumnScope.Content(
    state: State,
    loadConfig: (fileName: String) -> Config,
    saveConfig: (config: Config, fileName: String) -> Unit,
    sendToBadge: () -> Unit,
    stateUpdated: (State) -> Unit,
) {
    Row(
        modifier = Modifier.weight(1.0f),
    ) {
        when (state) {
            is Undecided -> UndecidedEditor(
                selectFile = {
                    val fileName = selectImageFile()
                    if (fileName.isImageFile()) {
                        stateUpdated(
                            EditImage(
                                configFileName = "$fileName.json",
                                image = ImageIO.read(
                                    File(fileName.orEmpty()),
                                ).toComposeImageBitmap(),
                            ),
                        )
                    }
                },

                createNameBadge = {
                    stateUpdated(
                        EditNameBadge(
                            name = "my name",
                            contact = "me@platform",
                            nameFontSize = 14,
                        ),
                    )
                },
            )

            is EditNameBadge -> NameBadgeEditor(
                state = state,
                sendToBadge = sendToBadge,
                stateUpdated = stateUpdated,
            )

            is EditImage -> ImageEditor(
                configFileName = state.configFileName,
                image = state.image,
                load = loadConfig,
                save = saveConfig,
                sendToBadge = sendToBadge,
            )

        }
    }
}

private val allowedImageExtensions = listOf("jpg", "jpeg", "png", "bmp")

private fun String?.isImageFile(): Boolean = if (this == null) {
    false
} else {
    val f = File(this)
    f.exists()
            && f.isFile
            && allowedImageExtensions.firstOrNull { endsWith(it) } != null
}

private fun selectImageFile(): String? {
    val dialog = FileDialog(null as Frame?, "select image to open (${allowedImageExtensions.joinToString(separator = ",")})")
    dialog.mode = FileDialog.LOAD
    dialog.isVisible = true
    return dialog.files.firstOrNull()?.absolutePath
}

@Composable
fun RowScope.UndecidedEditor(selectFile: () -> Unit, createNameBadge: () -> Unit) {
    Column(
        modifier = Modifier.weight(1.0f),
    ) {
        Spacer(modifier = Modifier.weight(1.0f))
        Button(
            modifier = Modifier.weight(1.0f).padding(16.dp),
            onClick = createNameBadge,
        ) {
            Spacer(modifier = Modifier.weight(1.0f))
            Text("Create Name Badge")
            Spacer(modifier = Modifier.weight(1.0f))
        }
        Button(
            modifier = Modifier.weight(1.0f).padding(16.dp),
            onClick = selectFile,
        ) {
            Spacer(modifier = Modifier.weight(1.0f))
            Text("Load image")
            Spacer(modifier = Modifier.weight(1.0f))
        }
        Spacer(modifier = Modifier.weight(1.0f))
    }
}

@Composable
private fun Footer(platform: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            fontSize = 10.sp,
            text = "~~ Running on with ❤️ on $platform. ~~",
        )
    }
}

