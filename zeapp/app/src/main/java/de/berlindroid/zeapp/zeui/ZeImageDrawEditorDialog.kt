package de.berlindroid.zeapp.zeui

import android.view.ViewGroup
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
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
import de.berlindroid.zeapp.zemodels.ZeConfiguration
import kotlinx.coroutines.launch

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
    accepted: (config: ZeConfiguration.ImageDraw) -> Unit = {},
    snackbarMessage: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // recomposition is triggered by reassigning the same path object
    var path by remember { mutableStateOf(Path(), policy = neverEqualPolicy()) }

    val drawContainer = remember {
        ComposeView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            setContent {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(PAGE_WIDTH / PAGE_HEIGHT.toFloat())
                        .clipToBounds()
                        .background(Color.White)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    path = path.apply { moveTo(offset.x, offset.y) }
                                },
                                onDrag = { pointerInputChange: PointerInputChange, offset: Offset ->
                                    val currentPosition = pointerInputChange.position + offset
                                    path = path.apply {
                                        lineTo(currentPosition.x, currentPosition.y)
                                    }
                                }
                            )
                        },
                ) {
                    drawPath(
                        color = Color.Black,
                        path = path,
                        style = Stroke(
                            width = 4.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
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
                            accepted(ZeConfiguration.ImageDraw(bitmap))
                        } else {
                            snackbarMessage(context.getString(R.string.not_binary_image))
                        }
                    }
                },
            ) {
                Text(stringResource(id = android.R.string.ok))
            }
        },
        title = {
            Text(stringResource(id = R.string.draw_image_page))
        },
        text = {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(PAGE_WIDTH / PAGE_HEIGHT.toFloat())
                    .clipToBounds()
                    .background(Color.Green),
                factory = {
                    drawContainer
                },
            )
        },
    )
}
