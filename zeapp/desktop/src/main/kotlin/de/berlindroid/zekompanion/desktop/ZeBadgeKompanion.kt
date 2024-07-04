@file:JvmName("ZeBadgeKompanion")

package de.berlindroid.zekompanion.desktop


import androidx.compose.material.MaterialTheme
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import de.berlindroid.zekompanion.base64
import de.berlindroid.zekompanion.buildBadgeManager
import de.berlindroid.zekompanion.desktop.ui.DrawNameBadge
import de.berlindroid.zekompanion.desktop.ui.ImageEditorBadge
import de.berlindroid.zekompanion.desktop.ui.ZeDesktopApp
import de.berlindroid.zekompanion.desktop.ui.fromJSONtoConfig
import de.berlindroid.zekompanion.ditherFloydSteinberg
import de.berlindroid.zekompanion.resize
import de.berlindroid.zekompanion.toBinary
import de.berlindroid.zekompanion.BADGE_WIDTH
import de.berlindroid.zekompanion.BADGE_HEIGHT
import de.berlindroid.zekompanion.BadgePayload.*
import de.berlindroid.zekompanion.zipit
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import java.awt.Robot
import java.awt.image.BufferedImage
import java.io.File
import java.nio.IntBuffer
import javax.swing.JFrame

fun main() = application {
    val painter = painterResource("icon.png")

    MaterialTheme {
        Window(
            title = "ZeBadge - Kompanion",
            icon = painter,
            onCloseRequest = ::exitApplication,
        ) {
            ZeDesktopApp(
                sendToBadge = { state ->
                    when (state) {
                        is State.EditNameBadge ->
                            sendImageToBadge(
                                image = state.toBufferedImage(),
                                callback = ::sendResult,
                            )

                        is State.EditImage ->
                            sendImageToBadge(
                                image = state.toBufferedImage(),
                                callback = ::sendResult,
                            )

                        is State.Undecided -> Unit
                    }
                },
            )
        }
    }
}

private fun sendResult(result: Result<Int>) {
    if (result.isFailure) {
        result.exceptionOrNull()?.printStackTrace()
    } else {
        println("Successfully sent: ${result.getOrNull() ?: "unknown"} bytes.")
    }
}

private fun State.EditImage.toBufferedImage(): BufferedImage {
    val img = BufferedImage(BADGE_WIDTH, BADGE_HEIGHT, BufferedImage.TYPE_INT_RGB)
    val g = img.createGraphics()

    val badge = ComposePanel()
    badge.size = Dimension(BADGE_WIDTH * 2, BADGE_HEIGHT * 2)
    badge.setContent {
        ImageEditorBadge(
            image,
            File(configFileName).readText().fromJSONtoConfig(),
        )
    }

    return with(JFrame("badge")) {
        layout = BorderLayout()
        defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        size = Dimension(BADGE_WIDTH, BADGE_HEIGHT)
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
                center.x - BADGE_WIDTH / 2,
                center.y - BADGE_HEIGHT / 2,
                BADGE_WIDTH,
                BADGE_HEIGHT,
            ),
        ).also {
            dispose()
        }
    }
}

private fun State.EditNameBadge.toBufferedImage(): BufferedImage {
    val img = BufferedImage(BADGE_WIDTH, BADGE_HEIGHT, BufferedImage.TYPE_INT_RGB)
    val g = img.createGraphics()

    val badge = ComposePanel()
    badge.size = Dimension(BADGE_WIDTH * 2, BADGE_HEIGHT * 2)
    badge.setContent {
        DrawNameBadge(
            state = this,
            factor = 2.0f,
            /** Hand waving of high def displays **/
        )
    }

    return with(JFrame("badge")) {
        layout = BorderLayout()
        defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        size = Dimension(BADGE_WIDTH, BADGE_HEIGHT)
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
                center.x - BADGE_WIDTH / 2,
                center.y - BADGE_HEIGHT / 2,
                BADGE_WIDTH,
                BADGE_HEIGHT,
            ),
        ).also {
            dispose()
        }
    }
}

private fun sendImageToBadge(image: BufferedImage, callback: (Result<Int>) -> Unit) {
    try {
        GlobalScope.launch {
            with(buildBadgeManager("")) {
                if (isConnected()) {
                    val payload = PreviewPayload(
                        debug = false,
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
        .resize(width, height, BADGE_WIDTH, BADGE_HEIGHT)
        .ditherFloydSteinberg(BADGE_WIDTH, BADGE_HEIGHT)
        .toBinary()
        .zipit()
        .base64()
}
