package de.berlindroid.zekompanion.desktop

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import de.berlindroid.zekompanion.BadgePayload
import de.berlindroid.zekompanion.base64
import de.berlindroid.zekompanion.buildBadgeManager
import de.berlindroid.zekompanion.desktop.ui.DrawBadge
import de.berlindroid.zekompanion.desktop.ui.ZeDesktopApp
import de.berlindroid.zekompanion.ditherFloydSteinberg
import de.berlindroid.zekompanion.resize
import de.berlindroid.zekompanion.toBinary
import de.berlindroid.zekompanion.zipit
import kotlinx.coroutines.runBlocking
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import java.awt.Robot
import java.awt.image.BufferedImage
import java.lang.IllegalStateException
import java.nio.IntBuffer
import javax.swing.JFrame


@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
    ) {
        ZeDesktopApp(
            sendToBadge = { state ->
                when (state) {
                    is State.EditNameBadge -> {
                        sendImageToBadge(
                            image = state.toBufferedImage(),
                            callback = ::sendResult,
                        )
                    }

                    is State.EditImage ->
                        sendImageToBadge(
                            image = state.image.toAwtImage(),
                            callback = ::sendResult,
                        )

                    is State.Undecided -> Unit
                }
            },
        )
    }
}

private fun sendResult(result: Result<Int>) {
    if (result.isFailure) {
        result.exceptionOrNull()?.printStackTrace()
    } else {
        println("Successfully sent: ${result.getOrNull() ?: "unknown"} bytes.")
    }
}

private fun State.EditNameBadge.toBufferedImage(): BufferedImage {
    val img = BufferedImage(296, 128, BufferedImage.TYPE_INT_RGB)
    val g = img.createGraphics()

    val badge = ComposePanel()
    badge.size = Dimension(296 * 2, 128 * 2)
    badge.setContent {
        DrawBadge(
            state = this,
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

private fun sendImageToBadge(image: BufferedImage, callback: (Result<Int>) -> Unit) {
    try {
        runBlocking {
            with(buildBadgeManager("")) {
                if (isConnected()) {
                    val payload = BadgePayload(
                        debug = false,
                        type = "preview",
                        meta = "",
                        image.toPayload(),
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
