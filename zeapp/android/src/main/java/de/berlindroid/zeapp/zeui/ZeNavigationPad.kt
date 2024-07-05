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
fun ZeNavigationPad(lazyListState: LazyListState) {
    val coroutineScope = rememberCoroutineScope()

    val topReached by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset == 0
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.End,
    ) {
        ZeFloatingScroller(
            coroutineScope = coroutineScope,
            lazyListState = lazyListState,
            direction =
                if (topReached) {
                    LazyListScrollDirections.DOWN
                } else {
                    LazyListScrollDirections.UP
                },
        )
        Spacer(modifier = Modifier.size(16.dp))
    }
}
