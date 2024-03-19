package de.berlindroid.zekompanion.server.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Operation {
    @Serializable
    @SerialName("FloydSteinberg")
    data class FloydSteinberg(val width: Int, val height: Int) : Operation()

    @Serializable
    @SerialName("Resize")
    data class Resize(val width: Int, val height: Int) : Operation()

    @Serializable
    @SerialName("Threshold")
    data class Threshold(val threshold: Int) : Operation()

    @Serializable
    @SerialName("Invert")
    data object Invert : Operation()

    @Serializable
    @SerialName("Grayscale")
    data object Grayscale : Operation()
}
