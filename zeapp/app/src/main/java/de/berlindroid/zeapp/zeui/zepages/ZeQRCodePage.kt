package de.berlindroid.zeapp.zeui.zepages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import de.berlindroid.zeapp.PAGE_HEIGHT
import de.berlindroid.zeapp.PAGE_WIDTH
import de.berlindroid.zeapp.zeui.rememberQrBitmapPainter

/**
 * A composable that represents a badge, to be rendered as a bitmap.
 */
@Composable
@Preview
fun QRCodePage(
    title: String = "Hello, my github profile is",
    text: String = "gdg-berlin-android",
    url: String = "https://github.com/gdg-berlin-android",
) {
    val pageWidth = with(LocalDensity.current) { PAGE_WIDTH.toDp() }
    val pageHeight = with(LocalDensity.current) { PAGE_HEIGHT.toDp() }
    Row(
        modifier = Modifier
            .background(
                color = Color.White,
            )
            .size(
                width = pageWidth,
                height = pageHeight
            ),
    ) {
        Column(
            modifier = Modifier
                .width(pageWidth - pageHeight)
                .fillMaxHeight()
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black),
                fontFamily = FontFamily.Cursive,
                fontSize = 12.sp,
                color = Color.White,
                maxLines = 3,
                text = title,
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                color = Color.Black,
                text = text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        val painter =
            if (url.isEmpty()) remember { ColorPainter(Color.White) }
            else rememberQrBitmapPainter(url)
        Image(
            painter = painter,
            contentDescription = "",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(pageHeight),
        )
    }
}
