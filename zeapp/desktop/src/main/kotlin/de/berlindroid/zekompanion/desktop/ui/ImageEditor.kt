package de.berlindroid.zekompanion.desktop.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.lang.String.format

@Serializable
data class Config(
    var name: String = "Ze Badger",

    var textHorizontalPadding: Int = 0,
    var textVerticalPadding: Int = 0,
    var textWidth: Int = 100,
    var textHeight: Int = 30,
    var textSize: Int = 30,

    var textForeground: Long = 0xFF000000,
    var textBackground: Long = 0x00FFFFFF,
)

fun Config.toJSON(): String =
    Json.encodeToString(Config.serializer(), this)


fun String.fromJSONtoConfig(): Config =
    Json.decodeFromString(Config.serializer(), this)


@Composable
fun ImageEditor(
    configFileName: String,
    image: ImageBitmap,
    save: (config: Config, fileName: String) -> Unit,
    load: (fileName: String) -> Config,
    sendToBadge: () -> Unit,
) {
    var config by remember { mutableStateOf(Config()) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
    ) {
        item {
            ImageEditorBadge(image, config)
        }

        item {
            Row {
                Column {
                    TextField(
                        value = config.name,
                        label = { Text("Enter your name") },
                        onValueChange = { config = config.copy(name = it) },
                    )

                    TextField(
                        value = config.textHorizontalPadding.toString(),
                        label = { Text("Enter textHorizontalPadding") },
                        onValueChange = { config = config.copy(textHorizontalPadding = (it.toIntOrNull() ?: 0)) },
                    )

                    TextField(
                        value = config.textVerticalPadding.toString(),
                        label = { Text("Enter textVerticalPadding") },
                        onValueChange = { config = config.copy(textVerticalPadding = (it.toIntOrNull() ?: 0)) },
                    )

                    TextField(
                        value = config.textWidth.toString(),
                        label = { Text("Enter text width.") },
                        onValueChange = { config = config.copy(textWidth = (it.toIntOrNull() ?: 0)) },
                    )

                    TextField(
                        value = config.textHeight.toString(),
                        label = { Text("Enter text height.") },
                        onValueChange = { config = config.copy(textHeight = (it.toIntOrNull() ?: 0)) },
                    )
                }

                Spacer(modifier = Modifier.size(4.dp))

                Column {
                    TextField(
                        value = config.textSize.toString(),
                        label = { Text("Enter text size") },
                        onValueChange = { config = config.copy(textSize = (it.toIntOrNull() ?: 0)) },
                    )

                    TextField(
                        value = format("0x%x", config.textForeground),
                        label = { Text("Enter text foreground color.") },
                        onValueChange = {
                            it.split("x").lastOrNull()?.let { n ->
                                config = config.copy(textForeground = (n.toLongOrNull(radix = 16) ?: 0))
                            }
                        },
                    )

                    TextField(
                        value = format("0x%x", config.textBackground),
                        label = { Text("Enter text background color.") },
                        onValueChange = {
                            it.split("x").lastOrNull()?.let { n ->
                                config = config.copy(textBackground = (n.toLongOrNull(radix = 16) ?: 0))
                            }
                        },
                    )

                    Button(
                        onClick = {
                            save(config, configFileName)
                        },
                    ) {
                        Text("Save configuration")
                    }
                    Button(
                        onClick = {
                            config = load(configFileName)
                        },
                    ) {
                        Text("Load configuration")
                    }
                }
            }

        }

        item {
            ImageManipulators {
                sendToBadge()
            }

        }
    }
}

@Composable
fun ImageEditorBadge(
    image: ImageBitmap,
    config: Config,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier.weight(1.0f),
        ) {
            Image(
                bitmap = image,
                contentDescription = null,
            )

            Text(
                modifier = Modifier
                    .padding(
                        horizontal = config.textHorizontalPadding.dp,
                        vertical = config.textVerticalPadding.dp,
                    ).width(config.textWidth.dp)
                    .height(config.textHeight.dp)
                    .background(Color(config.textBackground)),
                color = Color(config.textForeground),
                text = config.name,
                textAlign = TextAlign.Center,
                fontSize = config.textSize.sp,
                maxLines = 1,
                minLines = 1,
            )
        }
    }
}

