package de.berlindroid.zeapp.zeui


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NavigationPad(
    lazyListState: LazyListState
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollLength = 425f
    Column {
        ZeVloatingScroller(coroutineScope, lazyListState, -scrollLength, "↑")
        Spacer(modifier = Modifier.size(10.dp))
        ZeVloatingScroller(coroutineScope, lazyListState, scrollLength, "↓")
    }
}