package de.berlindroid.zeapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.R

/**
 * Simple Icon Button used for inside editor dialogs.
 */
@Composable
@Preview
fun ToolButton(
    painter: Painter = painterResource(id = R.drawable.ic_call_decline),
    text: String = "Decline",
    onClick: () -> Unit = {}
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.wrapContentHeight(),
    ) {
        Column(
            modifier = Modifier.wrapContentHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                tint = Color.Unspecified,
                painter = painter,
                contentDescription = null,
            )
            Text(
                text = text,
                fontSize = 12.sp
            )
        }
    }
}


/**
 * Simple Icon Button used for inside editor dialogs.
 */
@Composable
@Preview
fun ToolButton(
    imageVector: ImageVector = Icons.Filled.Send,
    text: String = "Send",
    onClick: () -> Unit = {}
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.wrapContentHeight(),
    ) {
        Column(
            modifier = Modifier.wrapContentHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                tint = Color.Unspecified,
                imageVector = imageVector,
                contentDescription = null,
            )
            Text(
                text = text,
                fontSize = 12.sp
            )
        }
    }
}