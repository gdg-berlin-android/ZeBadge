package de.berlindroid.zeapp.ui.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.berlindroid.zeapp.PAGE_HEIGHT
import de.berlindroid.zeapp.PAGE_WIDTH
import de.berlindroid.zeapp.R

@Composable
@Preview
fun NamePage(
    name: String = "Jane Doe",
    contact: String = "jane.doe@berlindroid.de",
) {
    Column(
        modifier = Modifier
            .background(
                color = Color.White,
            )
            .size(
                width = with(LocalDensity.current) { PAGE_WIDTH.toDp() },
                height = with(LocalDensity.current) { PAGE_HEIGHT.toDp() },
            )
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF801000)),
            fontFamily = FontFamily.SansSerif,
            fontSize = 8.sp,
            textAlign = TextAlign.Center,
            color = Color.White,
            maxLines = 1,
            text = "Hello, my name is",
        )
        Spacer(modifier = Modifier.weight(1.0f))
        Row(
            modifier = Modifier.padding(horizontal = 1.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier
                    .padding(1.dp),
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                text = name,
            )
            Spacer(modifier = Modifier.weight(1.0f))
            Image(
                modifier = Modifier
                    .height(
                        height = with(LocalDensity.current) { (PAGE_HEIGHT / 2).toDp() },
                    ),
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = null
            )

        }
        Text(
            modifier = Modifier.fillMaxWidth(),
            fontFamily = FontFamily.Monospace,
            fontSize = 4.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            text = contact,
        )
        Spacer(modifier = Modifier.weight(1.0f))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF801000)),
            fontFamily = FontFamily.SansSerif,
            fontSize = 3.sp,
            textAlign = TextAlign.Center,
            color = Color.White,
            maxLines = 1,
            text = "powered by berlindroid",
        )
    }
}