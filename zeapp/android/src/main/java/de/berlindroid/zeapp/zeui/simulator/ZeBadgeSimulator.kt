package de.berlindroid.zeapp.zeui.simulator

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.zebits.scaleIfNeeded
import de.berlindroid.zeapp.zeui.BinaryBitmapPageProvider
import de.berlindroid.zekompanion.BADGE_HEIGHT
import de.berlindroid.zekompanion.BADGE_WIDTH
import timber.log.Timber
import kotlin.math.min

/**
 * This is the simulator composable for the badge.
 *
 * Use it to simulate the badge and show the a given page in the composable.
 */
@Composable
@Preview(device = "spec:parent=pixel_3a_xl,orientation=landscape", showSystemUi = true)
fun BadgeSimulator(
    @PreviewParameter(BinaryBitmapPageProvider::class, 1) page: Bitmap,
    modifier: Modifier = Modifier,
    onButtonPressed: (ZeSimulatorButtonAction) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .paint(
                painterResource(id = R.drawable.badgerrpi2040),
                contentScale = ContentScale.Fit,
            ),
    ) {
        Spacer(Modifier.fillMaxHeight(.2f))
        Row(Modifier.fillMaxHeight(.76f)) {
            Spacer(modifier = Modifier.weight(1.5f))
            val drawState = rememberDrawingState(key = Unit, size = IntSize(BADGE_WIDTH, BADGE_HEIGHT))

            // Use this bitmap later to store/send to badge/send to list/etc
            // drawState.bitmap

            Image(
                modifier = Modifier
                    .width(550.dp)
                    .fillMaxHeight()
                    .drawing(drawState),
                bitmap = page
                    .scaleIfNeeded(BADGE_WIDTH, BADGE_HEIGHT)
                    .asImageBitmap(),
                contentDescription = null,
                filterQuality = FilterQuality.None,
            )
            Column {
                Spacer(modifier = Modifier.weight(1.0f))
                Text(
                    text = "⏺️",
                    fontSize = 43.sp,
                    modifier = Modifier.clickable { onButtonPressed(ZeSimulatorButtonAction.UP) },
                )
                Spacer(modifier = Modifier.weight(1.0f))
                Text(
                    text = "⏺️",
                    fontSize = 43.sp,
                    modifier = Modifier.clickable { onButtonPressed(ZeSimulatorButtonAction.DOWN) },
                )
                Spacer(modifier = Modifier.weight(1.0f))
            }
            Spacer(modifier = Modifier.weight(1.0f))
        }
        Spacer(Modifier.fillMaxHeight(.1f))
        Row {
            Spacer(modifier = Modifier.weight(1.0f))
            Text(
                text = "⬅️",
                fontSize = 43.sp,
                modifier = Modifier.clickable(onClick = { onButtonPressed(ZeSimulatorButtonAction.BACKWARD) }),
            )
            Spacer(modifier = Modifier.weight(1.0f))
            Text(
                text = "➡️️",
                fontSize = 43.sp,
                modifier = Modifier.clickable(onClick = { onButtonPressed(ZeSimulatorButtonAction.FORWARD) }),
            )
            Spacer(modifier = Modifier.weight(1.0f))
        }
    }
}

@Composable
private fun rememberDrawingState(key: Any, size: IntSize): DrawingState {
    val bitmap = ImageBitmap(
        size.width,
        size.height,
        config = ImageBitmapConfig.Argb8888,
    )
    val canvas = Canvas(bitmap)
    return remember(key, size) {
        DrawingState(
            size = size,
            bitmap = bitmap,
            canvas = canvas,
        )
    }
}

private class DrawingState(
    /** size of the badge */
    val size: IntSize,
    /** this bitmap holds the pixels to be applied to the badge */
    val bitmap: ImageBitmap,
    /** this canvas draws onto the bitmap */
    val canvas: Canvas,
) {

    /** this bitmap (and its canvas) its used only for the preview on the android device */
    private var _drawBitmap: ImageBitmap? = null
    private var _drawCanvas: Canvas? = null

    val drawBitmap: ImageBitmap
        get() = _drawBitmap!!
    val drawCanvas: Canvas
        get() = _drawCanvas!!

    fun ContentDrawScope.ensureDraw(): Boolean {
        if (_drawBitmap == null && size.isEmpty().not()) {
            _drawBitmap = ImageBitmap(
                size.width.toInt(),
                size.height.toInt(),
                config = ImageBitmapConfig.Argb8888,
            )
            _drawCanvas = Canvas(_drawBitmap!!)
        }

        return _drawCanvas != null
    }
}

private val paint = Paint().apply {
    color = Color.Black
    isAntiAlias = false
    strokeCap = StrokeCap.Square
    strokeWidth = 1f
}

private val drawPaint = Paint().apply {
    color = Color.Black
    isAntiAlias = false
    strokeCap = StrokeCap.Square
    strokeWidth = 30f
}

@SuppressLint("ReturnFromAwaitPointerEventScope")
@Composable
private fun Modifier.drawing(state: DrawingState): Modifier {
    var invalidate by remember { mutableIntStateOf(0) }
    var lastPosition by remember {
        mutableStateOf<Offset?>(null)
    }
    return this
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val pointerEvent = awaitPointerEvent()
                    val event = pointerEvent.changes.first()
                    val localLastPosition = lastPosition

                    // scaled down dot added to the [bitmap] to be sent to the badge
                    val scaleX = size.width / state.size.width.toFloat()
                    val scaleY = size.height / state.size.height.toFloat()

                    val point = Offset(event.position.x / scaleX, event.position.y / scaleY)
                    if (localLastPosition == null) {
                        state.canvas.drawPoints(PointMode.Points, listOf(point), paint)
                    } else {
                        val lastPoint = Offset(localLastPosition.x / scaleX, localLastPosition.y / scaleY)
                        state.canvas.drawPoints(PointMode.Lines, listOf(lastPoint, point), paint)
                    }

                    // scaled up dot added to be show on the device screen
                    drawPaint.strokeWidth = min(scaleX, scaleY)
                    if (localLastPosition == null) {
                        state.drawCanvas.drawPoints(PointMode.Points, listOf(event.position), drawPaint)
                    } else {
                        state.drawCanvas.drawPoints(PointMode.Lines, listOf(localLastPosition, event.position), drawPaint)
                    }

                    lastPosition = event.position.takeUnless {
                        pointerEvent.type == PointerEventType.Exit || pointerEvent.type == PointerEventType.Release
                    }

                    // this update++ is a hack
                    // I could not find a way to invalidate the drawing directly,
                    // so the Timber.d below logs the update,
                    // and by doing it forces Compose to re-draw the content.
                    invalidate++
                }
            }
        }
//        .drawWithCache {
//            // I found some invalidateDraw() and invalidateDrawCache() inside this but couldn´t access
//            // until then, the Timber log is working
//            onDrawWithContent {
//                drawContent()
//            }
//        }
        .drawWithContent {
            val canDraw = with(state) { ensureDraw() }
            drawContent()

            if (canDraw.not()) {
                return@drawWithContent
            }

            // DO NOT REMOTE THIS COMMENT, it does not work without it
            Timber.d("Drawing: update $invalidate.")

            // DEBUG only code:
            // This will draw the bitmap for the badge onto the screen

//            val scaleX = size.width / state.size.width.toFloat()
//            val scaleY = size.height / state.size.height.toFloat()
//            scale(scaleX, scaleY, pivot = Offset.Zero) {
//                drawImage(state.bitmap)
//            }

            drawImage(state.drawBitmap)
        }
}
