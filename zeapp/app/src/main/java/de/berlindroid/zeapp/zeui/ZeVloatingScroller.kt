package de.berlindroid.zeapp.zeui


import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
    text: String
) {
    FloatingActionButton(
        containerColor = Color.Black,
        onClick = {
            coroutineScope.launch {
                lazyListState.animateScrollBy(scrollLength)
            }
        }) {
        ZeText(
            text = text,
            color = Color.White,
            fontSize = 40.sp,
            textAlign = TextAlign.Center
        )
    }
}
