package de.berlindroid.zeapp.zeui.zeopensource

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.m3.LibraryDefaults.libraryColors

@Composable
fun ZeOpenSource(
    paddingValues: PaddingValues,
) {
    LibrariesContainer(
        Modifier.fillMaxSize(),
        contentPadding = paddingValues,
        colors = libraryColors(
            backgroundColor = MaterialTheme.colorScheme.surface,
            badgeBackgroundColor = MaterialTheme.colorScheme.primary,
            dialogConfirmButtonColor = MaterialTheme.colorScheme.primary,
        ),
    )
}
