package de.berlindroid.zeapp.zeui

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import de.berlindroid.zeapp.PAGE_HEIGHT
import de.berlindroid.zeapp.PAGE_WIDTH
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.ZeDimen
import de.berlindroid.zeapp.zebits.copy
import de.berlindroid.zekompanion.ditherFloydSteinberg
import de.berlindroid.zekompanion.ditherPositional
import de.berlindroid.zekompanion.ditherStaticPattern
import de.berlindroid.zekompanion.invert
import de.berlindroid.zekompanion.map
import de.berlindroid.zekompanion.rgb
import de.berlindroid.zekompanion.threshold
import java.nio.IntBuffer

/**
 * Embeddable editor that let's the user turn a given bitmap into a binary image
 *
 * @param bitmap the initial bitmap to be converted
 * @param bitmapUpdated update callback to be called when the bitmap is updated.
 */
@Composable
@Preview
fun BinaryImageEditor(
    @PreviewParameter(BinaryBitmapPageProvider::class, 1)
    bitmap: Bitmap,
    bitmapUpdated: (Bitmap) -> Unit = {},
) {
    var last by remember { mutableStateOf<Bitmap?>(null) }

    Column {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(unbounded = false)
                .height(PAGE_HEIGHT.dp)
                .padding(horizontal = ZeDimen.One, vertical = ZeDimen.Half),
            painter = BitmapPainter(
                image = bitmap.asImageBitmap(),
                filterQuality = FilterQuality.None,
            ),
            contentScale = ContentScale.FillWidth,
            contentDescription = null,
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = ZeDimen.Quarter),
            horizontalArrangement = Arrangement.End,
        ) {
            if (last != null) {
                item {
                    ToolButton(
                        painter = painterResource(id = R.drawable.binary_bitmap_modificator_undo),
                        text = stringResource(id = R.string.reset),
                        onClick = {
                            bitmapUpdated(last!!)
                            last = null
                        },
                    )
                }
            }
            item {
                ToolButton(
                    painter = painterResource(id = R.drawable.binary_bitmap_modificator_threshold),
                    text = stringResource(id = R.string.black_and_white),
                ) {
                    if (last == null) {
                        last = bitmap.copy()
                    }
                    bitmapUpdated(bitmap.pixelManipulation { _, _ -> threshold() })
                }
            }
            item {
                ToolButton(
                    painter = painterResource(id = R.drawable.binary_bitmap_modificator_floyd_steinberg),
                    text = stringResource(id = R.string.floyd_steninberg_initials),
                ) {
                    if (last == null) {
                        last = bitmap.copy()
                    }

                    bitmapUpdated(bitmap.pixelManipulation { w, h -> ditherFloydSteinberg(w, h) })
                }
            }
            item {
                ToolButton(
                    painter = painterResource(id = R.drawable.binary_bitmap_modificator_static),
                    text = stringResource(id = R.string.static_tool),
                ) {
                    if (last == null) {
                        last = bitmap.copy()
                    }

                    bitmapUpdated(bitmap.pixelManipulation { w, h -> ditherStaticPattern(w, h) })
                }
            }
            item {
                ToolButton(
                    painter = painterResource(id = R.drawable.binary_bitmap_modificator_positional),
                    text = stringResource(id = R.string.positional),
                ) {
                    if (last == null) {
                        last = bitmap.copy()
                    }
                    bitmapUpdated(bitmap.pixelManipulation { w, h -> ditherPositional(w, h) })
                }
            }
            item {
                ToolButton(
                    painter = painterResource(id = R.drawable.binary_bitmap_modificator_invert),
                    text = stringResource(id = R.string.invert),
                ) {
                    if (last == null) {
                        last = bitmap.copy()
                    }
                    bitmapUpdated(bitmap.pixelManipulation { _, _ -> invert() })
                }
            }
        }
    }
}

fun Bitmap.pixelManipulation(manipulator: IntBuffer.(width: Int, height: Int) -> IntBuffer): Bitmap {
    val input = IntBuffer.allocate(width * height)
    copyPixelsToBuffer(input)
    input.map { Color.valueOf(it).toArgb() }
    input.rewind()

    val output = input.manipulator(width, height)
    output.map {
        val (r, g, b) = it.rgb()
        Color.rgb(r, g, b)
    }
    output.rewind()
    copyPixelsFromBuffer(output)

    return this
}

fun Bitmap.pixelBuffer(): IntBuffer {
    val buffer = IntBuffer.allocate(width * height)
    copyPixelsToBuffer(buffer)
    buffer.map { Color.valueOf(it).toArgb() }
    buffer.rewind()

    return buffer
}

class BinaryBitmapPageProvider : PreviewParameterProvider<Bitmap> {
    override val values: Sequence<Bitmap>
        get() {
            return sequenceOf(
                with(Bitmap.createBitmap(PAGE_WIDTH, PAGE_HEIGHT, Bitmap.Config.ARGB_8888)) {
                    this
                },
            )
        }
}
