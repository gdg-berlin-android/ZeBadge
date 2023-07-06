package de.berlindroid.zeapp.zeui


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
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
    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.End) {
        ZeVloatingScroller(coroutineScope, lazyListState, -scrollLength, "↑")
        Spacer(modifier = Modifier.size(10.dp))
        ZeVloatingScroller(coroutineScope, lazyListState, scrollLength, "↓")
    }
}