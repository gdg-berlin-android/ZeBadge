package de.berlindroid.zeapp.zeui

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.Text as ZeText

@Composable
fun ZeVloatingScroller(
    coroutineScope: CoroutineScope,
    lazyListState: LazyListState,
    scrollLength: Float,
    text: String,
) {
    FloatingActionButton(
        containerColor = MaterialTheme.colorScheme.secondary,
        onClick = {
            coroutineScope.launch {
                lazyListState.animateScrollBy(scrollLength)
            }
        },
    ) {
        ZeText(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 40.sp,
            textAlign = TextAlign.Center,
        )
    }
}
