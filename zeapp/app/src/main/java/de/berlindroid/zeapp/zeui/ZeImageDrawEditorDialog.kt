package de.berlindroid.zeapp.zeui

import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.drawToBitmap
import de.berlindroid.zeapp.PAGE_HEIGHT
import de.berlindroid.zeapp.PAGE_WIDTH
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.zebits.isBinary
import de.berlindroid.zeapp.zebits.scaleIfNeeded
import de.berlindroid.zeapp.zebits.threshold
import de.berlindroid.zeapp.zevm.ZeBadgeViewModel
import kotlinx.coroutines.launch

enum class MotionEvent {
    Idle, Down, Move, Up
}


/**
 * Use Canvas to draw something with a finger on the badge.
 *
 * @param initialPrompt the initial prompt to be used to generate an image.
 * @param dismissed callback called when the editor dialog is dismissed
 * @param accepted callback called when the image is accepted
 */
@ExperimentalMaterial3Api
@Preview
@Composable
fun ZeImageDrawEditorDialog(
    initialPrompt: String = "Unicorn at an android conference in isometric view.",
    dismissed: () -> Unit = {},
    accepted: (config: ZeBadgeViewModel.Configuration.ImageDraw) -> Unit = {},
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var motionEvent by remember { mutableStateOf(MotionEvent.Idle) }
    // This is our motion event we get from touch motion
    var currentPosition by remember { mutableStateOf(Offset.Unspecified) }

    // Path is what is used for drawing line on Canvas
    val path = remember { Path() }

    val drawContainer = remember {

        ComposeView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setContent {
                LaunchedEffect(key1 = currentPosition, block = {
                    if (motionEvent == MotionEvent.Down) {
                        path.moveTo(currentPosition.x, currentPosition.y)
                    } else if (motionEvent == MotionEvent.Move) {
                        path.lineTo(currentPosition.x, currentPosition.y)
                    } else if (motionEvent == MotionEvent.Up) {
                        path.lineTo(currentPosition.x, currentPosition.y)
                    }
                })
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(PAGE_WIDTH / PAGE_HEIGHT.toFloat())
                        .clipToBounds()
                        .background(Color.White)
                        .pointerInput(Unit) {

                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPosition = offset
                                    Log.d("Drag", "Start")
                                    motionEvent = MotionEvent.Down
                                },
                                onDrag = { pointerInputChange: PointerInputChange, offset: Offset ->
                                    currentPosition =
                                        pointerInputChange.position + offset
                                    Log.d("Drag", "Move $currentPosition")
                                    motionEvent = MotionEvent.Move
                                },
                                onDragEnd = {
                                    Log.d("Drag", "Up")
                                    motionEvent = MotionEvent.Up
                                },
                                onDragCancel = {
                                    Log.d("Drag", "Up")
                                    motionEvent = MotionEvent.Up
                                },
                            )
                        }
                ) {
                    drawPath(
                        color = Color.Black,
                        path = path,
                        style = Stroke(
                            width = 4.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
    }
    AlertDialog(
        onDismissRequest = dismissed,
        confirmButton = {
            Button(
                onClick = {
                    coroutineScope.launch {
                        val bitmap =
                            drawContainer
                                .drawToBitmap()
                                .scaleIfNeeded(PAGE_WIDTH, PAGE_HEIGHT)
                                .threshold()

                        if (bitmap.isBinary()) {
                            accepted(ZeBadgeViewModel.Configuration.ImageDraw(bitmap))
                        } else {
                            Toast.makeText(context, R.string.not_binary_image, Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                }) {
                Text(stringResource(id = android.R.string.ok))
            }
        },
        title = {
            Text(stringResource(id = R.string.draw_image_page))
        },
        text = {
            AndroidView(modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(PAGE_WIDTH / PAGE_HEIGHT.toFloat())
                .clipToBounds()
                .background(Color.Green),
                factory = {
                    drawContainer
                })
        }
    )
}

