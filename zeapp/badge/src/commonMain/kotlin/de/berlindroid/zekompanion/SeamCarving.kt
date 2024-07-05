package de.berlindroid.zekompanion

import de.berlindroid.zekompanion.PathDirection.HORIZONTAL
import de.berlindroid.zekompanion.PathDirection.VERTICAL
import java.nio.IntBuffer
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


/**
 * Carve least significant pixels out of an image, making the size fit..
 *
 * First: reduce image size respecting the aspect ratio, and then go carve the rest.
 *
 * See {@see [seam carving](https://en.wikipedia.org/wiki/Seam_carving)}
 *
 * Falls back to returning the input image if increasing size is requested.
 *
 * TODO: Add increasing the size by duplicating
 */
fun IntBuffer.carve(inputWidth: Int, inputHeight: Int, outputWidth: Int, outputHeight: Int): IntBuffer {
    if (outputHeight > inputHeight || outputWidth > inputWidth) {
        return this
    }

    val widthDiff = inputWidth - outputWidth
    val heightDiff = inputHeight - outputHeight

    var intermittend = copy()
    for (i in 0 until widthDiff) {
        val path = intermittend.findMinimalPixelPath(
            width = inputWidth - i,
            height = inputHeight,
            direction = VERTICAL,
        )

        intermittend = intermittend.carvePath(
            inputWidth = inputWidth - i,
            inputHeight = inputHeight,
            outputWidth = inputWidth - i - 1,
            outputHeight = inputHeight,
            path = path,
            direction = HORIZONTAL,
        )
    }

    for (i in 0 until heightDiff) {
        val path = intermittend.findMinimalPixelPath(
            width = outputWidth,
            height = inputHeight - i,
            direction = HORIZONTAL,
        )

        intermittend = intermittend.carvePath(
            inputWidth = inputWidth - widthDiff,
            inputHeight = inputHeight - i,
            outputWidth = outputWidth,
            outputHeight = inputHeight - i - 1,
            path = path,
            direction = VERTICAL,
        )
    }

    return intermittend
}

private enum class PathDirection {
    VERTICAL, HORIZONTAL
}

private data class Coordinate(val x: Int, val y: Int)

private data class MinimumProducer(
    val dimensions: Pair<Int, Int>,
    val initialCoordinate: (value: Int) -> Coordinate,
    val nextCoordinates: (source: Coordinate) -> Array<Coordinate>,
)

private fun IntBuffer.findMinimalPixelPath(width: Int, height: Int, direction: PathDirection): List<Coordinate> {
    val (dimensions, initialCoordinate, nextCoordinates) = when (direction) {
        VERTICAL -> MinimumProducer(
            width to height,
            { Coordinate(it, 0) },
            {
                arrayOf(
                    Coordinate(it.x - 1, it.y + 1),
                    Coordinate(it.x, it.y + 1),
                    Coordinate(it.x + 1, it.y + 1),
                )
            },
        )

        HORIZONTAL -> MinimumProducer(
            height to width,
            { Coordinate(0, it) },
            {
                arrayOf(
                    Coordinate(it.x + 1, it.y - 1),
                    Coordinate(it.x + 1, it.y),
                    Coordinate(it.x + 1, it.y + 1),
                )
            },
        )

    }

    val paths = mutableListOf<Pair<Int, List<Coordinate>>>()
    for (i in 0 until dimensions.first) {
        val path = mutableListOf(initialCoordinate(i))
        var distance = 0

        // carve
        for (j in 1 until dimensions.second) {
            val last = path.last()
            val (nextDistance, next) = minimalDiff(
                width = width,
                source = last,
                *nextCoordinates(last),
            )

            distance += nextDistance
            path.add(next)
        }

        paths.add(distance to path)
    }

    val optimalPath = paths.minBy { it.first }
    return optimalPath.second
}

private fun IntBuffer.minimalDiff(width: Int, source: Coordinate, vararg candidates: Coordinate): Pair<Int, Coordinate> {
    val sourceColor = at(source, width)

    var minimalCandidate = candidates.first()
    var minimalDifference = Int.MAX_VALUE

    for (candidate in candidates) {
        val candidateColor = at(candidate, width)

        val difference = colorCompare(candidateColor.rgb(), sourceColor.rgb())
        if (minimalDifference > difference) {
            minimalDifference = difference
            minimalCandidate = candidate
        }

    }

    return minimalDifference to minimalCandidate
}

private fun colorCompare(a: List<Int>, b: List<Int>): Int =
    (0.3 * sq(b[0] - a[0]) + 0.59 * sq(b[1] - a[1]) + 0.11 * sq(b[2] - a[2])).roundToInt()

private fun sq(a: Int) = a * a

private fun IntBuffer.at(c: Coordinate, width: Int): Int =
    if (c.x in 0 until width && c.y in 0 until limit() / width) {
        get(c.x + c.y * width)
    } else {
        Int.MAX_VALUE
    }

private data class CarveProducer(
    val outputDimensions: Pair<Int, Int>,

    val lastIndexOfItemToCopy: (pathIndex: Int) -> Int,

    val inputIndex: (i: Int, j: Int) -> Int,
    val outputIndex: (i: Int, j: Int) -> Int,
)

private fun limit(x: Int, maximum: Int) = min(maximum, max(0, x))

private fun IntBuffer.carvePath(
    inputWidth: Int,
    inputHeight: Int,
    outputWidth: Int,
    outputHeight: Int,
    path: List<Coordinate>,
    direction: PathDirection,
): IntBuffer {
    val (outputDimensions, lastIndexToCopy, inputIndex, outputIndex) = when (direction) {
        HORIZONTAL ->
            CarveProducer(
                outputDimensions = outputWidth to outputHeight,
                lastIndexOfItemToCopy = { i -> path[i].x },
                inputIndex = { x, y -> limit(x, inputWidth) + limit(y, inputHeight) * inputWidth },
                outputIndex = { x, y -> limit(x, outputWidth) + limit(y, outputHeight) * outputWidth },
            )

        VERTICAL ->
            CarveProducer(
                outputDimensions = outputHeight to outputWidth,
                lastIndexOfItemToCopy = { i -> path[i].y },
                inputIndex = { y, x -> limit(x, inputWidth) + limit(y, inputHeight) * inputWidth },
                outputIndex = { y, x -> limit(x, outputWidth) + limit(y, outputHeight) * outputWidth },
            )
    }

    val output = IntBuffer.allocate(outputWidth * outputHeight)
    for (j in 0 until outputDimensions.second) {
        // copy
        val carveToIndex = lastIndexToCopy(j)
        for (i in 0 until carveToIndex) {
            val inIndex = inputIndex(i, j)
            val outIndex = outputIndex(i, j)

            output.put(outIndex, get(inIndex))
        }

        // carve
        for (k in carveToIndex until outputDimensions.first) {
            val inIndex = inputIndex(k + 1, j)
            val outIndex = outputIndex(k, j)

            output.put(outIndex, get(inIndex))
        }
    }

    return output
}
