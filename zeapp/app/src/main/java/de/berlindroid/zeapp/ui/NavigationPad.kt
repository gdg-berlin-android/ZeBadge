package de.berlindroid.zeapp.ui


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope

@Composable
fun NavigationPad(
    coroutineScope: CoroutineScope,
    lazyListState: LazyListState
) {
    val scrollLength = 425f
    Column {
        ZeVloatingScroller(coroutineScope, lazyListState, -scrollLength, "↑")
        Spacer(modifier = Modifier.size(10.dp))
        ZeVloatingScroller(coroutineScope, lazyListState, scrollLength, "↓")
    }
}