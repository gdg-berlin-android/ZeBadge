package de.berlindroid.zeapp.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.bits.ditherFloydSteinberg
import de.berlindroid.zeapp.bits.ditherStaticPattern
import de.berlindroid.zeapp.bits.invert
import de.berlindroid.zeapp.bits.threshold

@Composable
fun BinaryImageEditor(
    bitmap: Bitmap,
    refresh: () -> Unit,
    bitmapUpdated: (Bitmap) -> Unit
) {
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
    Row {
        Spacer(modifier = Modifier.weight(1.0f))
        IconButton(onClick = refresh) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null
            )
        }
        IconButton(
            onClick = { bitmapUpdated(bitmap.threshold()) },
        ) {
            Icon(
                tint = Color.Unspecified,
                modifier = Modifier.border(
                    width = 1.dp,
                    color = LocalContentColor.current
                ),
                painter = painterResource(id = R.drawable.binary_bitmap_modificator_threshold),
                contentDescription = null
            )
        }
        IconButton(
            onClick = { bitmapUpdated(bitmap.ditherFloydSteinberg()) }
        ) {
            Icon(
                tint = Color.Unspecified,
                modifier = Modifier.border(
                    width = 1.dp,
                    color = LocalContentColor.current
                ),
                painter = painterResource(id = R.drawable.binary_bitmap_modificator_floid_steinberg),
                contentDescription = null
            )
        }
        IconButton(
            onClick = { bitmapUpdated(bitmap.ditherStaticPattern()) }
        ) {
            Icon(
                tint = Color.Unspecified,
                modifier = Modifier.border(
                    width = 1.dp,
                    color = LocalContentColor.current
                ),
                painter = painterResource(id = R.drawable.binary_bitmap_modificator_static),
                contentDescription = null
            )
        }
        IconButton(
            onClick = { bitmapUpdated(bitmap.invert()) }
        ) {
            Icon(
                tint = Color.Unspecified,
                modifier = Modifier.border(
                    width = 1.dp,
                    color = LocalContentColor.current
                ),
                painter = painterResource(id = R.drawable.binary_bitmap_modificator_invert),
                contentDescription = null
            )
        }
    }
}