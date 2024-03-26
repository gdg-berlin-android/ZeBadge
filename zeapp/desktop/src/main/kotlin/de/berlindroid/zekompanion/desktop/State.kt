package de.berlindroid.zekompanion.desktop

import androidx.compose.ui.graphics.ImageBitmap

sealed class State {
    data object Undecided : State()
    data class EditNameBadge(
        val name: String,
        val contact: String,
        val nameFontSize: Int,
    ) : State()

    data class EditImage(
        val configFileName: String,
        val image: ImageBitmap,
    ) : State()
}
