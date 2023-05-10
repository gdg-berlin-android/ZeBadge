package de.berlindroid.zeapp.ui

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
import de.berlindroid.zeapp.bits.ditherFloydSteinberg
import de.berlindroid.zeapp.bits.ditherPositional
import de.berlindroid.zeapp.bits.ditherStaticPattern
import de.berlindroid.zeapp.bits.invert
import de.berlindroid.zeapp.bits.randomizeColors
import de.berlindroid.zeapp.bits.threshold

@Composable
@Preview
fun BinaryImageEditor(
    @PreviewParameter(BinaryBitmapPageProvider::class, 1)
    bitmap: Bitmap,
    bitmapUpdated: (Bitmap) -> Unit = {}
) {
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
            item {
                ToolButton(
                    painter = painterResource(id = R.drawable.binary_bitmap_modificator_threshold),
                    text = "B/W"
                ) { bitmapUpdated(bitmap.threshold()) }
            }
            item {
                ToolButton(
                    painter = painterResource(id = R.drawable.binary_bitmap_modificator_floyd_steinberg),
                    text = "FS Dither"
                ) { bitmapUpdated(bitmap.ditherFloydSteinberg()) }
            }
            item {
                ToolButton(
                    painter = painterResource(id = R.drawable.binary_bitmap_modificator_static),
                    text = "Static Dither"
                ) { bitmapUpdated(bitmap.ditherStaticPattern()) }
            }
            item {
                ToolButton(
                    painter = painterResource(id = R.drawable.binary_bitmap_modificator_positional),
                    text = "Positional Dither"
                ) { bitmapUpdated(bitmap.ditherPositional()) }
            }
            item {
                ToolButton(
                    painter = painterResource(id = R.drawable.binary_bitmap_modificator_invert),
                    text = "Invert"
                ) { bitmapUpdated(bitmap.invert()) }
            }
        }
    }
}

class BinaryBitmapPageProvider : PreviewParameterProvider<Bitmap> {
    override val values: Sequence<Bitmap>
        get() {
            return sequenceOf(
                with(Bitmap.createBitmap(PAGE_WIDTH, PAGE_HEIGHT, Bitmap.Config.ARGB_8888)) {
                    randomizeColors()
                    this
                }
            )
        }
}
