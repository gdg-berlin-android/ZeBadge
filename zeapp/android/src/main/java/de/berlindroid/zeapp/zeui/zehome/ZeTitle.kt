package de.berlindroid.zeapp.zeui.zehome

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import de.berlindroid.zeapp.R

@Composable
internal fun ZeTitle(
    modifier: Modifier = Modifier,
    titleClick: () -> Unit,
) {
    Text(
        modifier = modifier.clickable {
            titleClick()
        },
        style = MaterialTheme.typography.titleLarge,
        text = buildAnnotatedString {
            pushStyle(SpanStyle(fontWeight = FontWeight.Black))
            append(stringResource(id = R.string.app_name).take(2))
            pop()
            pushStyle(SpanStyle(fontWeight = FontWeight.Normal))
            append(stringResource(id = R.string.app_name).drop(2))
            pop()
        },
    )
}
