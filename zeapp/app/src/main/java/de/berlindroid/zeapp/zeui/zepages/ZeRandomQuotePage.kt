package de.berlindroid.zeapp.zeui.zepages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import de.berlindroid.zeapp.PAGE_HEIGHT
import de.berlindroid.zeapp.PAGE_WIDTH

/**
 * A composable that represents a quote card, to be renderd as a bitmap.
 */
@Composable
@Preview
fun RandomQuotePage(
    message: String = "Don't blame others. it won't make you a better person.",
    author: String = "Lolly Daskal",
) {
    val size by remember { mutableStateOf(6.sp) }

    Column(
        modifier = Modifier
            .background(
                color = Color.White,
            )
            .size(
                width = with(LocalDensity.current) { PAGE_WIDTH.toDp() },
                height = with(LocalDensity.current) { PAGE_HEIGHT.toDp() },
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black),
            fontFamily = FontFamily.Monospace,
            fontSize = 8.sp,
            textAlign = TextAlign.Center,
            color = Color.White,
            maxLines = 1,
            text = "Quote of The Day",
        )
        Text(
            modifier = Modifier
                .wrapContentSize()
                .weight(1.0f),
            fontSize = size,
            textAlign = TextAlign.Center,
            text = message,
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black),
            fontFamily = FontFamily.Monospace,
            fontSize = 6.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            text = author,
        )
    }
}