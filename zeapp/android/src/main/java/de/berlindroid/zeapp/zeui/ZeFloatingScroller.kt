package de.berlindroid.zeapp.zeui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text as ZeText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.berlindroid.zeapp.zeui.zetheme.ZeBlack
import de.berlindroid.zeapp.zeui.zetheme.ZeWhite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ZeFloatingScroller(
    coroutineScope: CoroutineScope,
    lazyListState: LazyListState,
    direction: LazyListScrollDirections,
) {
    val text =
        when (direction) {
            LazyListScrollDirections.UP -> "↑"
            LazyListScrollDirections.DOWN -> "↓"
        }

    FloatingActionButton(
        containerColor = ZeBlack,
        modifier =
            Modifier
                .padding(16.dp)
                .border(
                    width = 1.dp,
                    color = ZeWhite,
                    shape = RoundedCornerShape(16.dp),
                ),
        onClick = {
            coroutineScope.launch {
                when (direction) {
                    LazyListScrollDirections.UP -> lazyListState.animateScrollToItem(0)
                    LazyListScrollDirections.DOWN ->
                        lazyListState.animateScrollToItem(
                            lazyListState.layoutInfo.totalItemsCount - 1,
                        )
                }
            }
        },
    ) {
        ZeText(
            text = text,
            color = ZeWhite,
            fontSize = 32.sp,
            textAlign = TextAlign.Center,
        )
    }
}
