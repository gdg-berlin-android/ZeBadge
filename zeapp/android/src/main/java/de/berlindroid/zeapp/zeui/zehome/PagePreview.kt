package de.berlindroid.zeapp.zeui.zehome

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.ZeDimen
import de.berlindroid.zeapp.zeui.BinaryBitmapPageProvider
import de.berlindroid.zeapp.zeui.ToolButton
import de.berlindroid.zeapp.zeui.zetheme.ZeWhite

@Composable
@Preview
@Suppress("LongParameterList")
internal fun PagePreview(
    @PreviewParameter(BinaryBitmapPageProvider::class, 1)
    bitmap: Bitmap,
    modifier: Modifier = Modifier,
    name: String = "",
    customizeThisPage: (() -> Unit)? = null,
    resetThisPage: (() -> Unit)? = null,
    sendToDevice: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier
            .padding(ZeDimen.Quarter),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = ZeWhite,
        ),
        border = BorderStroke(1.dp, ZeWhite),
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(unbounded = true)
                .padding(horizontal = ZeDimen.One, vertical = ZeDimen.One)
                .clip(RoundedCornerShape(4.dp)),
            painter = BitmapPainter(
                image = bitmap.asImageBitmap(),
                filterQuality = FilterQuality.None,
            ),
            contentScale = ContentScale.FillWidth,
            contentDescription = null,
        )

        Row {
            Text(
                text = name,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = ZeDimen.One),
            )
            if (resetThisPage != null || customizeThisPage != null || sendToDevice != null) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = ZeDimen.Quarter),
                    horizontalArrangement = Arrangement.End,
                ) {
                    if (sendToDevice != null) {
                        item {
                            ToolButton(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                text = stringResource(id = R.string.send),
                                onClick = sendToDevice,
                            )
                        }
                    }
                    if (resetThisPage != null) {
                        item {
                            ToolButton(
                                imageVector = Icons.Filled.Refresh,
                                text = stringResource(id = R.string.reset),
                                onClick = resetThisPage,
                            )
                        }
                    }
                    if (customizeThisPage != null) {
                        item {
                            ToolButton(
                                imageVector = Icons.Filled.Edit,
                                text = stringResource(id = R.string.ze_edit),
                                onClick = customizeThisPage,
                            )
                        }
                    }
                }
            }
        }
    }
}
