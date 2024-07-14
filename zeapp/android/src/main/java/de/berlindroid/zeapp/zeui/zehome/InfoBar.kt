package de.berlindroid.zeapp.zeui.zehome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.ZeDimen
import de.berlindroid.zeapp.zeui.zetheme.ZeBlack
import de.berlindroid.zeapp.zeui.zetheme.ZeCarmine
import de.berlindroid.zeapp.zeui.zetheme.ZeWhite

/**
 * Show a message to the user with a timer ticking down.
 */
@Composable
@Preview
internal fun InfoBar(
    modifier: Modifier = Modifier,
    message: String = stringResource(id = R.string.ze_very_important),
    progress: Float = 0.5f,
    copyMoreToClipboard: (() -> Unit) = {},
) {
    Card(
        modifier = modifier
            .padding(horizontal = ZeDimen.One, vertical = ZeDimen.One)
            .background(ZeCarmine, RoundedCornerShape(ZeDimen.One))
            .zIndex(10.0f),
        colors =
        CardDefaults.cardColors(
            containerColor = ZeCarmine,
            contentColor = ZeWhite,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = ZeDimen.Two, vertical = ZeDimen.One),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1.0f),
                fontSize = 20.sp,
                fontFamily = FontFamily.Monospace,
                color = ZeWhite,
                text = message,
            )

            IconButton(onClick = copyMoreToClipboard) {
                Icon(
                    painter =
                    painterResource(
                        id = R.drawable.copy_clipboard,
                    ),
                    contentDescription = "Copy info bar message",
                )
            }
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 0.dp, bottom = 4.dp),
            color = ZeBlack,
            trackColor = ZeWhite,
            strokeCap = StrokeCap.Round,
        )
    }
}
