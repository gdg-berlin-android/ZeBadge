package de.berlindroid.zeapp.zealteregos.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import de.berlindroid.zeapp.zealteregos.vm.AlterEgosVm
import de.berlindroid.zeapp.zealteregos.vm.UiState
import de.berlindroid.zeapp.zealteregos.vm.User

@Composable
@Preview
fun ZeAlterEgos(paddingValues: PaddingValues) {
    val viewModel: AlterEgosVm = hiltViewModel()

    val state by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        if (state.users.isEmpty()) {
            EmptyView(paddingValues)
        } else {
            AllEgosView(paddingValues, state, viewModel)

            state.selectedUser?.let { user ->
                DetailedUserView(paddingValues, user) {
                    viewModel.userClicked(null)
                }

                BackHandler {
                    viewModel.userClicked(null)
                }
            }
        }
    }
}

@Composable
@Preview
private fun AllEgosView(
    paddingValues: PaddingValues,
    state: UiState,
    viewModel: AlterEgosVm,
) {
    LazyVerticalGrid(
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
        columns = GridCells.Adaptive(200.dp),
    ) {
        items(state.users) { user ->
            Box(
                modifier =
                    Modifier.clickable {
                        viewModel.userClicked(user.uuid)
                    },
            ) {
                AsyncImage(
                    modifier = Modifier.size(200.dp),
                    model = user.smallPngUrl,
                    contentDescription = user.description,
                    alignment = Alignment.Center,
                )
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(Color(0, 0, 0, 128))
                            .padding(vertical = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun EmptyView(paddingValues: PaddingValues) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = paddingValues.calculateTopPadding()),
    ) {
        Spacer(
            modifier = Modifier.weight(1.0f),
        )
        Text(
            style = MaterialTheme.typography.headlineLarge,
            text = "Please wait, talking to ZeServer.",
        )
        Spacer(
            modifier = Modifier.weight(1.0f),
        )
    }
}

@Composable
@Preview
private fun DetailedUserView(
    paddingValues: PaddingValues,
    user: User = User("UUID", "NAME", "PNG", "SMPNG", "DESC", "PHRASE"),
    clicked: () -> Unit = {},
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color(0, 0, 0, 200))
                .padding(
                    top = paddingValues.calculateTopPadding() + 16.dp,
                    bottom = paddingValues.calculateBottomPadding() + 16.dp,
                ).clickable { clicked() },
    ) {
        SubcomposeAsyncImage(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            model = user.pngUrl,
            loading = {
                AsyncImage(
                    model = user.smallPngUrl,
                    contentDescription = "please wait",
                )
            },
            contentDescription = user.description,
            alignment = Alignment.Center,
            contentScale = ContentScale.Fit,
        )

        Column(
            modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
        ) {
            Text(
                text = user.name,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            )

            Text(
                text = user.description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Justify,
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = user.chatPhrase,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Right,
            )
        }
    }
}
