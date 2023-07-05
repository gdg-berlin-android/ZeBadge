package de.berlindroid.zeapp.zeui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.berlindroid.zeapp.PAGE_HEIGHT
import de.berlindroid.zeapp.PAGE_WIDTH
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.zebits.scaleIfNeeded
import de.berlindroid.zeapp.vm.ZeBadgeViewModel.Slot

/**
 * This is the simulator composable for the badge.
 *
 * Use it to simulate the badge and show the a given page in the composable.
 */
@Composable
@Preview(device = "spec:parent=pixel_3a_xl,orientation=landscape", showSystemUi = true)
fun BadgeSimulator(
    @PreviewParameter(BinaryBitmapPageProvider::class, 1) page: Bitmap,
    onButtonPressed: (buttonForSlot: Slot) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .paint(
                painterResource(id = R.drawable.badgerrpi2040),
                contentScale = ContentScale.Fit,
            )
    ) {
        Spacer(Modifier.fillMaxHeight(.2f))
        Row(Modifier.fillMaxHeight(.76f)) {
            Spacer(modifier = Modifier.weight(1.5f))
            Image(
                modifier = Modifier
                    .width(550.dp)
                    .fillMaxHeight(),
                bitmap = page
                    .scaleIfNeeded(PAGE_WIDTH, PAGE_HEIGHT)
                    .asImageBitmap(),
                contentDescription = null,
                filterQuality = FilterQuality.None,
            )
            Column() {
                Spacer(modifier = Modifier.weight(1.0f))
                Text(
                    text = "⏺️",
                    fontSize = 43.sp,
                    modifier = Modifier.clickable { onButtonPressed(Slot.FirstCustom) }
                )
                Spacer(modifier = Modifier.weight(1.0f))
                Text(
                    text = "⏺️",
                    fontSize = 43.sp,
                    modifier = Modifier.clickable { onButtonPressed(Slot.SecondCustom) }
                )
                Spacer(modifier = Modifier.weight(1.0f))
            }
            Spacer(modifier = Modifier.weight(1.0f))
        }
        Spacer(Modifier.fillMaxHeight(.1f))
        Row {
            Spacer(modifier = Modifier.weight(1.0f))
            Text(
                text = "⏺️",
                fontSize = 43.sp,
                modifier = Modifier.clickable { onButtonPressed(Slot.Name) })
            Spacer(modifier = Modifier.weight(1.0f))
            Text(
                text = "⏺️",
                fontSize = 43.sp,
                modifier = Modifier.clickable { onButtonPressed(Slot.FirstSponsor) })
            Spacer(modifier = Modifier.weight(1.0f))
            Text(
                text = "⏺️",
                fontSize = 43.sp,
                modifier = Modifier.clickable { onButtonPressed(Slot.SecondSponsor) })
            Spacer(modifier = Modifier.weight(1.0f))
        }
    }
}