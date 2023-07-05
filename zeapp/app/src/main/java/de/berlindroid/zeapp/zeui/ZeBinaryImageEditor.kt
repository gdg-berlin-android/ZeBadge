package de.berlindroid.zeapp.zeui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import de.berlindroid.zeapp.PAGE_HEIGHT
import de.berlindroid.zeapp.PAGE_WIDTH
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.zebits.copy
import de.berlindroid.zeapp.zebits.ditherFloydSteinberg
import de.berlindroid.zeapp.zebits.ditherPositional
import de.berlindroid.zeapp.zebits.ditherStaticPattern
import de.berlindroid.zeapp.zebits.invert
import de.berlindroid.zeapp.zebits.threshold

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
    bitmapUpdated: (Bitmap) -> Unit = {}
) {
    var last by remember { mutableStateOf<Bitmap?>(null) }

    Column {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(unbounded = true)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            painter = BitmapPainter(
                image = bitmap.asImageBitmap(),
                filterQuality = FilterQuality.None,
            ),
            contentScale = ContentScale.FillWidth,
            contentDescription = null,
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 2.dp),
            horizontalArrangement = Arrangement.End
        ) {
            if (last != null) {
                item {
                    ToolButton(
                        painter = painterResource(id = R.drawable.binary_bitmap_modificator_undo),
                        text = "Reset",
                        onClick = {
                            bitmapUpdated(last!!)
                            last = null
                        }
                    )
                }
            }
            item {
                ToolButton(
                    painter = painterResource(id = R.drawable.binary_bitmap_modificator_threshold),
                    text = "B/W"
                ) {
                    if (last == null) {
                        last = bitmap.copy()
                    }
                    bitmapUpdated(bitmap.threshold())
                }
            }
            item {
                ToolButton(
                    painter = painterResource(id = R.drawable.binary_bitmap_modificator_floyd_steinberg),
                    text = "FS"
                ) {
                    if (last == null) {
                        last = bitmap.copy()
                    }

                    bitmapUpdated(bitmap.ditherFloydSteinberg())
                }
            }
            item {
                ToolButton(
                    painter = painterResource(id = R.drawable.binary_bitmap_modificator_static),
                    text = "Static"
                ) {
                    if (last == null) {
                        last = bitmap.copy()
                    }

                    bitmapUpdated(bitmap.ditherStaticPattern())
                }
            }
            item {
                ToolButton(
                    painter = painterResource(id = R.drawable.binary_bitmap_modificator_positional),
                    text = "Positional"
                ) {
                    if (last == null) {
                        last = bitmap.copy()
                    }
                    bitmapUpdated(bitmap.ditherPositional())
                }
            }
            item {
                ToolButton(
                    painter = painterResource(id = R.drawable.binary_bitmap_modificator_invert),
                    text = "Invert"
                ) {
                    if (last == null) {
                        last = bitmap.copy()
                    }
                    bitmapUpdated(bitmap.invert())
                }
            }
        }
    }
}

class BinaryBitmapPageProvider : PreviewParameterProvider<Bitmap> {
    override val values: Sequence<Bitmap>
        get() {
            return sequenceOf(
                with(Bitmap.createBitmap(PAGE_WIDTH, PAGE_HEIGHT, Bitmap.Config.ARGB_8888)) {
                    this
                }
            )
        }
}

