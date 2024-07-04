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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import de.berlindroid.zeapp.R
import de.berlindroid.zekompanion.BADGE_HEIGHT
import de.berlindroid.zekompanion.BADGE_WIDTH

/**
 * A composable that represents a badge, to be renderd as a bitmap.
 */
@Composable
@Preview
fun NamePage(
    name: String = stringResource(id = R.string.your_name_here),
    contact: String = stringResource(id = R.string.contact_me_here),
) {
    var size by remember { mutableStateOf(10.sp) }
    var secondarySize by remember { mutableStateOf(8.sp) }

    Column(
        modifier = Modifier
            .background(
                color = Color.White,
            )
            .size(
                width = with(LocalDensity.current) { BADGE_WIDTH.toDp() },
                height = with(LocalDensity.current) { BADGE_HEIGHT.toDp() },
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF801000)),
            fontFamily = FontFamily.Cursive,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            color = Color.White,
            maxLines = 1,
            text = stringResource(R.string.hello_my_name_is),
        )
        Text(
            modifier = Modifier
                .wrapContentSize()
                .weight(1.0f),
            fontSize = size,
            textAlign = TextAlign.Center,
            text = name,
            onTextLayout = { result ->
                if (result.didOverflowWidth) {
                    size /= 2.0f
                }
            },
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF801000)),
            fontFamily = FontFamily.Monospace,
            fontSize = secondarySize,
            color = Color.White,
            textAlign = TextAlign.Center,
            text = contact,
            onTextLayout = { result ->
                if (result.didOverflowWidth) {
                    secondarySize /= 3.0f
                }
            },
        )
    }
}

/**
 * Another preview
 */
@Composable
@Preview
@Suppress("UnusedPrivateMember")
private fun NamePagePreview2(
    name: String = "Your Name Here is very long",
    contact: String = "Contact Me Here is very long long",
) {
    NamePage(name, contact)
}
