package de.berlindroid.zekompanion.server.models

import kotlinx.serialization.Serializable

@Serializable
data class ImageRequest(
    val operations: List<Operation> = emptyList(),
    val image: String = "",
    val width: Int = -1,
    val height: Int = -1,
)
