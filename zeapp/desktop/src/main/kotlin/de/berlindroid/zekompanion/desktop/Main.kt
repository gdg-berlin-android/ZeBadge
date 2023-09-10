package de.berlindroid.zekompanion.desktop

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import de.berlindroid.zekompanion.BadgePayload
import de.berlindroid.zekompanion.base64
import de.berlindroid.zekompanion.buildBadgeManager
import de.berlindroid.zekompanion.ditherFloydSteinberg
import de.berlindroid.zekompanion.getPlatform
import de.berlindroid.zekompanion.resize
import de.berlindroid.zekompanion.toBinary
import de.berlindroid.zekompanion.zipit
import kotlinx.coroutines.runBlocking
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FileDialog
import java.awt.Frame
import java.awt.GraphicsEnvironment
import java.awt.Image
import java.awt.Rectangle
import java.awt.Robot
import java.awt.image.BufferedImage
import java.io.File
import java.lang.IllegalStateException
import java.nio.IntBuffer
import javax.imageio.ImageIO
import javax.swing.JFrame
import kotlin.math.max


sealed class State {
    data object Undecided : State()
    data class NameBadge(
        val name: String,
        val contact: String,
        val nameFontSize: Int,
    ) : State()

    data class Image(val image: ImageBitmap) : State()
}

@Composable
@Preview
fun App(sendToBadge: (State.NameBadge) -> Unit) {
    val platform = getPlatform()

    var state: State by remember { mutableStateOf(State.Undecided) }

    MaterialTheme {
        Column {
            Header(state) { state = it }

            Content(
                state = state,
                sendToBadge = sendToBadge,
                stateUpdated = { state = it },
            )

            Footer(platform)
        }
    }
}

@Composable
fun ColumnScope.Header(state: State, updateState: (State) -> Unit) {
    val goBack = @Composable {
        Button(
            onClick = {
                updateState(State.Undecided)
            },
        ) {
            Text("<")
        }
    }

    when (state) {
        is State.Image -> goBack()
        is State.NameBadge -> goBack()
        else -> Unit
    }
}

@Composable
fun ColumnScope.Content(state: State, sendToBadge: (State.NameBadge) -> Unit, stateUpdated: (State) -> Unit) {
    Row(
        modifier = Modifier.weight(1.0f),
    ) {
        when (state) {
            is State.Undecided -> EmptyView(
                selectFile = {
                    stateUpdated(selectImageFile(state))
                },
                createNameBadge = {
                    stateUpdated(State.NameBadge("my name", "me@platform", 14))
                },
            )

            is State.NameBadge -> NameBadge(
                state = state,
                sendToBadge = sendToBadge,
                stateUpdated = stateUpdated,
            )

            is State.Image -> Image(
                modifier = Modifier.weight(1.0f),
                bitmap = state.image,
                contentDescription = null,
            )

        }
    }
}

@Composable
private fun RowScope.NameBadge(
    state: State,
    sendToBadge: (State.NameBadge) -> Unit,
    stateUpdated: (State) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
    ) {
        DrawBadge(state, 5.0f)

        Spacer(modifier = Modifier.weight(1.0f))

        Row {
            TextField(
                modifier = Modifier.weight(1.0f),
                value = (state as State.NameBadge).name,
                onValueChange = {
                    stateUpdated(State.NameBadge(it, state.contact, state.nameFontSize))
                },
                label = { Text("Your name") },
            )

            TextField(
                modifier = Modifier.weight(1.0f),
                value = state.contact,
                onValueChange = {
                    stateUpdated(State.NameBadge(state.name, it, state.nameFontSize))
                },
                label = { Text("Your contact info") },
            )
        }

        Row {
            TextField(
                modifier = Modifier.weight(1.0f),
                value = (state as State.NameBadge).nameFontSize.toString(),
                onValueChange = {
                    stateUpdated(State.NameBadge(state.name, state.contact, it.toIntOrNull() ?: state.nameFontSize))
                },
                label = { Text("Text Size") },
            )
        }

        Row {
            Spacer(modifier = Modifier.weight(1.0f))
            Button(
                content = {
                    Text("send to badge")
                },
                onClick = {
                    sendToBadge(state as State.NameBadge)
                },
            )
        }
    }
}

private fun selectImageFile(state: State): State {
    val dialog = FileDialog(null as Frame?, "Select File to Open")
    dialog.mode = FileDialog.LOAD
    dialog.isVisible = true
    val file: String? = dialog.files.firstOrNull()?.absolutePath

    return if (file != null) {
        State.Image(ImageIO.read(File(file)).toComposeImageBitmap())
    } else {
        state
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun DrawBadge(state: State, factor: Float = 1.0f) {
    val measurer = rememberTextMeasurer()

    val density = LocalDensity.current
    val (dpWidth, dpHeight) = with(density) { 296.toDp() * factor to 128.toDp() * factor }

    Column {
        Canvas(
            modifier = Modifier.size(dpWidth, dpHeight).align(Alignment.CenterHorizontally),
        ) {
            val width = 296.0f * factor
            val height = 128.0f * factor

            val topBarHeight = 30.0f * factor
            val bottomBarHeight = 30.0f * factor

            val background = Color(255, 255, 255)
            val highlight = Color(255, 0, 0)

            // fill
            drawRect(color = background, size = Size(width, height))

            // bars
            drawRect(color = highlight, topLeft = Offset.Zero, size = Size(width, topBarHeight))
            drawRect(color = highlight, topLeft = Offset(0.0f, height - bottomBarHeight), size = Size(width, bottomBarHeight))

            // words
            drawCenteredText(
                measurer = measurer,
                text = "My name is",
                fontSize = 8.sp * factor,
                color = Color.White,
                width = width,
                height = topBarHeight,
                x = 0.0f,
                y = 0.0f,
            )

            drawCenteredText(
                measurer = measurer,
                text = (state as State.NameBadge).name,
                fontSize = state.nameFontSize.sp * factor,
                color = Color.Black,
                width = width,
                height = height - topBarHeight - bottomBarHeight,
                x = 0.0f,
                y = topBarHeight,
            )

            drawCenteredText(
                measurer = measurer,
                text = state.contact,
                fontSize = 8.sp * factor,
                color = Color.White,
                width = width,
                height = bottomBarHeight,
                x = 0.0f,
                y = height - bottomBarHeight,
            )
        }
    }
}

@Composable
fun RowScope.EmptyView(selectFile: () -> Unit, createNameBadge: () -> Unit) {
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
private fun ColumnScope.Footer(platform: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            fontSize = 10.sp,
            text = "~~ Running on platform $platform. ~~",
        )
    }
}

@ExperimentalTextApi
fun DrawScope.drawCenteredText(
    measurer: TextMeasurer,
    text: String,
    fontSize: TextUnit,
    color: Color,
    fontFamily: FontFamily = FontFamily.Serif,
    width: Float,
    height: Float,
    x: Float,
    y: Float,
) {
    val annotatedText = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                fontSize = fontSize,
                color = color,
                fontFamily = fontFamily,
            ),
        ) {
            append(text)
        }
    }

    val layoutResult = measurer.measure(
        text = annotatedText,
        overflow = TextOverflow.Ellipsis,
        maxLines = 2,
    ).constraintTo(width.toInt(), height.toInt())

    drawText(
        textLayoutResult = layoutResult,
        topLeft = Offset(
            max(x, width / 2.0f - layoutResult.size.width / 2.0f),
            y + max(0.0f, height / 2.0f - layoutResult.size.height / 2.0f),
        ),
    )

}

private fun TextLayoutResult.constraintTo(width: Int, height: Int): TextLayoutResult {
    return if (size.width > width || size.height > height) {
        copy(layoutInput, IntSize(width, height))
    } else {
        this
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
    ) {
        App(
            sendToBadge = { state ->
                sendImageToBadge(drawBadgeState(state)) {
                    if (it.isFailure) {
                        it.exceptionOrNull()?.printStackTrace()
                    } else {
                        println("Successfully sent: ${it.getOrNull() ?: "unknown"} bytes.")
                    }
                }
            },
        )
    }
}

fun drawBadgeState(state: State.NameBadge): BufferedImage {
    val img = BufferedImage(296, 128, BufferedImage.TYPE_INT_RGB)
    val g = img.createGraphics()

    val badge = ComposePanel()
    badge.size = Dimension(296 * 2, 128 * 2)
    badge.setContent {
        DrawBadge(
            state = state,
            factor = 2.0f,
            /** Hand waving of high def displays **/
        )
    }

    return with(JFrame("badge")) {
        layout = BorderLayout()
        defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        size = Dimension(296, 128)
        isAlwaysOnTop = true // make sure it is ontop, before we screenshot
        isUndecorated = true // no borders, no things we need to consider
        isVisible = true // also: visible for screenshot is important
        setLocationRelativeTo(null) // center on screen
        add(badge)

        paintAll(g)
        g.dispose()

        val center = GraphicsEnvironment.getLocalGraphicsEnvironment().centerPoint

        Robot().createScreenCapture(
            Rectangle(
                center.x - 296 / 2,
                center.y - 128 / 2,
                296,
                128,
            ),
        ).also {
            dispose()
        }
    }
}

private fun sendImageToBadge(badgeImage: BufferedImage, callback: (Result<Int>) -> Unit) {
    try {
        runBlocking {
            with(buildBadgeManager("")) {
                if (isConnected()) {
                    val payload = BadgePayload(
                        debug = false,
                        type = "preview",
                        meta = "",
                        badgeImage.toPayload(),
                    )

                    callback(sendPayload(payload))
                } else {
                    callback(
                        Result.failure(IllegalStateException("Not connected")),
                    )
                }
            }
        }
    } catch (e: Exception) {
        callback(
            Result.failure(UnknownError("")),
        )
    }
}

private fun BufferedImage.toPayload(): String {
    val array = IntArray(width * height * 3)
    getRGB(0, 0, width, height, array, 0, width)

    return IntBuffer
        .wrap(array)
        .resize(width, height, 296, 128)
        .ditherFloydSteinberg(296, 128)
        .toBinary()
        .zipit()
        .base64()
}

fun Image.toBufferedImage(): BufferedImage {
    if (this is BufferedImage) {
        return this
    }

    // Create a buffered image with transparency
    val image = BufferedImage(getWidth(null), getHeight(null), BufferedImage.TYPE_INT_ARGB)

    // Draw the image on to the buffered image
    val graphics = image.createGraphics()
    graphics.drawImage(this, 0, 0, null)
    graphics.dispose()

    // Return the buffered image
    return image
}

