package de.berlindroid.zeapp.zeui


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ZeNavigationPad(
    lazyListState: LazyListState
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollLength = 425f
    val topReached by remember { derivedStateOf { lazyListState.layoutInfo.visibleItemsInfo.firstOrNull()?.offset == 0 } }
    val bottomReached by remember { derivedStateOf { lazyListState.layoutInfo.visibleItemsInfo
        .lastOrNull()?.index == lazyListState.layoutInfo.totalItemsCount - 1 } }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(24.dp), horizontalAlignment = Alignment.End
    ) {
        if (!topReached) {
            ZeVloatingScroller(coroutineScope, lazyListState, -scrollLength, "↑")
        }
        Spacer(modifier = Modifier.size(10.dp))
        if (!bottomReached) {
            ZeVloatingScroller(coroutineScope, lazyListState, scrollLength, "↓")
        }
    }
}