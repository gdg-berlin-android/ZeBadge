package de.berlindroid.zeapp.zeui.zehome

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import de.berlindroid.zeapp.zevm.ZePassVm
import org.koin.androidx.compose.koinViewModel

@Composable
fun ZeUserProfile(paddingValues: PaddingValues) {
    val viewModel: ZePassVm = koinViewModel()

    val state by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = paddingValues.calculateTopPadding()),
            contentPadding =
                PaddingValues(
                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = paddingValues.calculateBottomPadding(),
                ),
        ) {
            item {
                Text(
                    text = "ZePass Chat Messages",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            items(state.messages) { message ->
                Row(
                    modifier = Modifier.padding(16.dp),
                ) {
                    AsyncImage(
                        model = message.profileUrl,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            style = MaterialTheme.typography.titleMedium,
                            text = message.userName,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            style = MaterialTheme.typography.bodyMedium,
                            text = message.text,
                        )
                    }
                }
            }
        }
    }
}
