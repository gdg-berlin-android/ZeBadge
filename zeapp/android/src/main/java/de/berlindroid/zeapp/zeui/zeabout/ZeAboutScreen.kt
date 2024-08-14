package de.berlindroid.zeapp.zeui.zeabout

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import coil.compose.AsyncImage
import de.berlindroid.zekompanion.getPlatform

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ZeAbout(
    paddingValues: PaddingValues,
    vm: ZeAboutViewModel = koinViewModel(),
) {
    val contributors by vm.lines.collectAsState()

    Surface(
        modifier =
            Modifier
                .fillMaxSize(),
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
            stickyHeader {
                Column(
                    Modifier
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                ) {
                    Text(
                        text = "Top ${contributors.count()} contributors",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 24.sp,
                    )
                    Text(
                        text = "Running on '${getPlatform()}'.",
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }
            items(contributors) { contributor ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AsyncImage(
                        model = contributor.imageUrl,
                        contentDescription = "avatar",
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .size(50.dp)
                                .clip(CircleShape),
                    )
                    Text(
                        text = "${contributor.name}: ${contributor.contributions}",
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 18.sp,
                    )
                }
            }
            item {
                LaunchedEffect(true) {
                    if (contributors.isNotEmpty()) {
                        vm.loadNextPage()
                    }
                }
            }
        }
    }
}
