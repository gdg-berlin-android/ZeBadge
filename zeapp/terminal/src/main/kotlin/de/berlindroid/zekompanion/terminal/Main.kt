package de.berlindroid.zekompanion.terminal

import de.berlindroid.zekompanion.BadgePayload
import de.berlindroid.zekompanion.base64
import de.berlindroid.zekompanion.buildBadgeManager
import de.berlindroid.zekompanion.ditherFloydSteinberg
import de.berlindroid.zekompanion.getPlatform
import de.berlindroid.zekompanion.grayscale
import de.berlindroid.zekompanion.invert
import de.berlindroid.zekompanion.resize
import de.berlindroid.zekompanion.resizeAndCarve
import de.berlindroid.zekompanion.threshold
import de.berlindroid.zekompanion.toBinary
import de.berlindroid.zekompanion.zipit
import de.berlindroid.zekompanion.BADGE_WIDTH
import de.berlindroid.zekompanion.BADGE_HEIGHT
import de.berlindroid.zekompanion.DeleteLoad
import de.berlindroid.zekompanion.PreviewLoad
import de.berlindroid.zekompanion.StoreLoad
import kotlinx.coroutines.runBlocking
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.File
import java.nio.IntBuffer
import java.util.UUID
import javax.imageio.ImageIO
import kotlin.system.exitProcess

const val COLOR_RED_BACKGROUND = "\u001B[41m"
const val COLOR_GREEN_BACKGROUND = "\u001B[42m"
const val COLOR_BLUE_BACKGROUND = "\u001B[44m"
const val COLOR_END = "\u001B[m"

data class CommandLineArgument(
    val name: String,
    val description: String,
    val short: String,
    val long: String,
    val hasParameter: Boolean = false,
    val callback: (it: String?) -> Unit,
)

typealias StorageOperation = () -> Unit
typealias ImageOperation = IntBuffer.(width: Int, height: Int) -> IntBuffer

data object Configuration {
    var input: String? = null
    var output: String? = null
    var width: Int = 0
    var height: Int = 0
    var storageOperations: MutableList<Pair<String, StorageOperation>> = mutableListOf()
    var imageOperations: MutableList<Pair<String, ImageOperation>> = mutableListOf()
}

fun inputOutputCommands() = mutableListOf(
    CommandLineArgument(
        name = "input image",
        description = "Read image to be manipulated.",
        short = "-i",
        long = "--input",
        hasParameter = true,
    ) { Configuration.input = it },
    CommandLineArgument(
        name = "output image",
        description = "Image manipulation results will get saved as the output image.",
        short = "-o",
        long = "--output",
        hasParameter = true,
    ) { Configuration.output = it },
    CommandLineArgument(
        name = "store image on badge",
        description = "Sends the hopefully converted image to the hopefully connected badge. Make sure to _not_ be in developer mode.",
        short = "-st",
        long = "--store",
        hasParameter = true,
    ) { filename ->
        Configuration.imageOperations.add(
            "store" to storeBufferOntoBadge(filename ?: UUID.randomUUID().toString()),
        )
    },
    CommandLineArgument(
        name = "preview image on badge",
        description = "Sends the hopefully converted image to the hopefully connected badge.",
        short = "-p",
        long = "--preview",
    ) {
        Configuration.imageOperations.add(
            "preview" to previewImageOnBadge(),
        )
    },
)

fun storageCommands() = listOf(
    CommandLineArgument(
        name = "shows help from the badge",
        description = "Asks the badge for available commands. Needs a badge attached.",
        short = "-H",
        long = "--badge-help",
    ) {
        Configuration.storageOperations.add(
            "help" to badgeHelp(),
        )
    },
    CommandLineArgument(
        name = "list images on badge",
        description = "Asks the badge which images are already stored on it. Take priority over other operations, cannot be used with input and " +
                "output operations.",
        short = "-l",
        long = "--list",
    ) {
        Configuration.storageOperations.add(
            "list" to listImagesStoredOnBadge(),
        )
    },
    CommandLineArgument(
        name = "show stored image",
        description = "Shows an already stored image on the badge.",
        short = "-s",
        long = "--show",
        hasParameter = true,
    ) { filename ->
        Configuration.storageOperations.add(
            "show" to showStoredImageOnBadge(filename),
        )
    },
    CommandLineArgument(
        name = "delete image from badge",
        description = "Deletes an existing badge image from the storage of zebadge.",
        short = "-d",
        long = "--delete",
        hasParameter = true,
    ) { filename ->
        Configuration.storageOperations.add(
            "delete" to deleteStoredImageOnBadge(filename),
        )
    },
)

fun imageColorCommands() = listOf(
    CommandLineArgument(
        name = "dither floyd-steinberg",
        description = "Converts the given input image into a black and white output image, somewhat preserving the luminance using dithering.",
        short = "-fs",
        long = "--floyd-steinberg",
    ) { Configuration.imageOperations.add("fs dither" to IntBuffer::ditherFloydSteinberg) },
    CommandLineArgument(
        name = "image threshold",
        description = "Thresholds the given input image. Value above the parameter are considered white, others black",
        short = "-t",
        long = "--threshold",
    ) { Configuration.imageOperations.add("threshold" to { _, _ -> threshold() }) },
    CommandLineArgument(
        name = "image inversion",
        description = "Turn black into white and opposite.",
        short = "-i",
        long = "--invert",
    ) { Configuration.imageOperations.add("invert" to { _, _ -> invert() }) },
    CommandLineArgument(
        name = "grayscale an image",
        description = "Convert image into a smooth transition from black to white color.",
        short = "-g",
        long = "--grayscale",
    ) { Configuration.imageOperations.add("gray" to { _, _ -> grayscale() }) },
)

fun resizeCommands() = listOf(
    CommandLineArgument(
        name = "resize image",
        description = "Simple resize of image into 'WIDTHxHEIGHT' image.",
        short = "-r",
        long = "--resize",
        hasParameter = true,
    ) { Configuration.imageOperations.add("resize" to resizeImageCallback(it)) },
    CommandLineArgument(
        name = "fluidly resize image",
        description = "Simple graph cut resize the image into 'WIDTHxHEIGHT' result image.",
        short = "-f",
        long = "--fluid",
        hasParameter = true,
    ) { Configuration.imageOperations.add("fluid" to resizeFluidImageCallback(it)) },
)

fun helpCommand() = CommandLineArgument(
    name = "help",
    description = "Helps you, overrides all other operations.",
    short = "-h",
    long = "--help",
) { help() }

val commands: List<CommandLineArgument> = listOf(
    helpCommand(),
    *inputOutputCommands().toTypedArray(),
    *storageCommands().toTypedArray(),
    *imageColorCommands().toTypedArray(),
    *resizeCommands().toTypedArray(),
)

private fun resizeImageCallback(size: String?): IntBuffer.(width: Int, height: Int) -> IntBuffer = { w, h ->
    val (ow, oh) = if (size != null && size.contains("x")) {
        try {
            size.split("x", limit = 2).map { it.toInt() }
        } catch (ill: IllegalArgumentException) {
            print("non number found")
            ill.printStackTrace()
            listOf(100, 100)
        }
    } else {
        listOf(BADGE_WIDTH, BADGE_HEIGHT)
    }

    resize(w, h, ow, oh).also {
        Configuration.width = ow
        Configuration.height = oh
    }
}

private fun resizeFluidImageCallback(size: String?): IntBuffer.(width: Int, height: Int) -> IntBuffer = { w, h ->
    val (ow, oh) = if (size != null && size.contains("x")) {
        try {
            size.split("x", limit = 2).map { it.toInt() }
        } catch (ill: IllegalArgumentException) {
            print("non number found")
            ill.printStackTrace()
            listOf(100, 100)
        }
    } else {
        listOf(BADGE_WIDTH, BADGE_HEIGHT)
    }

    resizeAndCarve(w, h, ow, oh).also {
        Configuration.width = ow
        Configuration.height = oh
    }
}


private fun storeBufferOntoBadge(filename: String): IntBuffer.(width: Int, height: Int) -> IntBuffer = { _, _ ->
    val payload = StoreLoad(
        debug = false,
        meta = filename,
        payload = toBinary().zipit().base64(),
    )

    runBlocking {
        with(buildBadgeManager("")) {
            if (isConnected()) {
                val result = sendPayload(payload)
                if (result.isSuccess) {
                    println("${COLOR_GREEN_BACKGROUND}Successfully stored image (${result.getOrNull()}).${COLOR_END}")
                } else {
                    println("${COLOR_RED_BACKGROUND}Image not stored.${COLOR_END}")
                }
            } else {
                println("${COLOR_RED_BACKGROUND}No Badge connected.${COLOR_END}\nTry attaching a badge and execute the command again.")
            }
        }
    }

    this
}

private fun listImagesStoredOnBadge(): StorageOperation = {
    rawCommand("list")
}

private fun badgeHelp(): StorageOperation = {
    rawCommand("help")
}

private fun showStoredImageOnBadge(filename: String?): StorageOperation = {
    rawCommand("show", filename ?: "")
}

private fun defaultTransformer(result: Result<String>): String =
    if (result.isSuccess) {
        ".. $COLOR_BLUE_BACKGROUND" +
                result.getOrDefault("").split(",").joinToString(separator = "$COLOR_END\n.. $COLOR_BLUE_BACKGROUND") +
                COLOR_END
    } else {
        "..${COLOR_RED_BACKGROUND}But no response from badge received.${COLOR_END}"
    } + "\n"

private fun rawCommand(
    command: String, meta: String = "", payload: String = "",
    resultTransformer: (result: Result<String>) -> String = ::defaultTransformer,
) {
    val badgePayload = BadgePayload(
        debug = false,
        type = command,
        meta = meta,
        payload = payload,
    )

    runBlocking {
        with(buildBadgeManager("")) {
            if (isConnected()) {
                val result = sendPayload(badgePayload)
                if (result.isSuccess) {
                    println(
                        COLOR_GREEN_BACKGROUND +
                                "Successfully send command '$command' with meta '$meta' and payload (#${payload.length}).\n" +
                                badgePayload.toBadgeCommand() + "\n" +
                                COLOR_END,
                    )

                    val readResult = readResponse()

                    println("Response:\n${resultTransformer(readResult)}")
                } else {
                    println("${COLOR_RED_BACKGROUND}Couldn't execute raw command.${COLOR_END}")
                }
            } else {
                println("${COLOR_RED_BACKGROUND}No Badge connected.${COLOR_END}\nTry attaching a badge and execute the command again.")
            }
        }
    }

}

private fun deleteStoredImageOnBadge(filename: String?): StorageOperation = {
    val payload = DeleteLoad(
        debug = false,
        meta = filename ?: "",
        payload = "",
    )

    runBlocking {
        with(buildBadgeManager("")) {
            if (isConnected()) {
                val result = sendPayload(payload)
                if (result.isSuccess) {
                    println("${COLOR_GREEN_BACKGROUND}Successfully deleted image '$filename' (${result.getOrNull()}).${COLOR_END}")
                } else {
                    println("${COLOR_RED_BACKGROUND}Couldn't delete image.${COLOR_END}")
                }
            } else {
                println("${COLOR_RED_BACKGROUND}No Badge connected.${COLOR_END}\nTry attaching a badge and execute the command again.")
            }
        }
    }
}

private fun previewImageOnBadge(): IntBuffer.(width: Int, height: Int) -> IntBuffer = { _, _ ->
    val payload = PreviewLoad(
        debug = false,
        meta = "",
        payload = toBinary().zipit().base64(),
    )

    runBlocking {
        with(buildBadgeManager("")) {
            if (isConnected()) {
                val result = sendPayload(payload)
                if (result.isSuccess) {
                    println("${COLOR_GREEN_BACKGROUND}Successfully sent image (${result.getOrNull()}).${COLOR_END}")
                    println(payload.toBadgeCommand())
                } else {
                    println("${COLOR_RED_BACKGROUND}Image not send.${COLOR_END}")
                }
            } else {
                println("${COLOR_RED_BACKGROUND}No Badge connected.${COLOR_END}\nTry attaching a badge and execute the command again.")
            }
        }
    }

    this
}

fun help() {
    println("Following arguments are supported:")
    commands.forEach { command ->
        val name = String.format("%-27s", "${command.short} | ${command.long}${if (command.hasParameter) " [param]" else ""}")
        println("$name ${command.name}: ${command.description}")
    }
    println()
    println("Either 'output' or 'send' needs to be present.")
    exitProcess(0)
}

fun main(userArgs: Array<String>) {
    val arguments = userArgs.toMutableList()
    println("Welcome to ${COLOR_GREEN_BACKGROUND}ZeBadge Kompanion Terminal${COLOR_END}")
    println(" ~~ using ${getPlatform()} ~~\n")

    parseArguments(arguments)

    handleCommands()

    println("${COLOR_GREEN_BACKGROUND}Thank you. Good Bye.${COLOR_END}")
}

private fun parseArguments(arguments: MutableList<String>) {
    while (arguments.isNotEmpty()) {
        val argument = arguments.removeFirst()
        val command = commands.firstOrNull { it.short == argument || it.long == argument }
        if (command == null) {
            println(
                "${COLOR_RED_BACKGROUND}Command for argument '$argument' not found.${COLOR_END}\n" +
                        "Trying raw serial command, in case you know more than me.",
            )

            val splits = argument.split(' ')
            val rawCommand = if (splits.size > 0) splits[0] else ""
            val meta = if (splits.size > 1) splits[1] else ""
            val payload = if (splits.size > 2) splits[2] else ""

            rawCommand(rawCommand, meta, payload)
            continue
        }

        if (command.hasParameter) {
            val parameter = arguments.removeFirstOrNull()
            if (parameter != null && parameter.startsWith("-")) {
                // next argument is not a parameter but a command
                arguments.add(0, parameter)
                command.callback(null)
            } else {
                command.callback(parameter)
            }
        } else {
            command.callback(null)
        }
    }
}

private fun handleCommands() {
    for (storage in Configuration.storageOperations) {
        storage.second()
    }
    if (!Configuration.input.isNullOrEmpty()) {
        // any other image manipulation operations
        if (noResultTarget()) {
            println("${COLOR_RED_BACKGROUND}No result operation specified, use output, send, store or show as a result of the operation..${COLOR_END}")
        } else {
            handleImageOperation()
        }
    }

}

private fun handleImageOperation() {
    val input = Configuration.input!!
    val inputImage = ImageIO.read(File(input))
    Configuration.width = inputImage.width
    Configuration.height = inputImage.height
    val width = Configuration.width
    val height = Configuration.height

    val array = IntArray(width * height * 3)
    inputImage.getRGB(0, 0, width, height, array, 0, width)

    var buffer = IntBuffer.wrap(array)

    println("Processing image '$input'.")

    Configuration.imageOperations.forEach { operation ->
        println("... ${operation.first}")
        buffer = buffer.(operation.second)(
            Configuration.width,
            Configuration.height,
        )
    }

    Configuration.output?.let { output ->
        val outputImage = BufferedImage(
            Configuration.width,
            Configuration.height,
            TYPE_INT_RGB,
        )

        outputImage.setRGB(0, 0, Configuration.width, Configuration.height, buffer.array(), 0, Configuration.width)
        ImageIO.write(outputImage, "png", File(output))

        println("Successfully saved image to '$output'.")
    }
}

private fun noResultTarget() =
    Configuration.output == null
            && Configuration.imageOperations.firstOrNull { it.first == "list" } == null
            && Configuration.imageOperations.firstOrNull { it.first == "send" } == null
            && Configuration.imageOperations.firstOrNull { it.first == "show" } == null
            && Configuration.imageOperations.firstOrNull { it.first == "store" } == null
            && Configuration.imageOperations.firstOrNull { it.first == "preview" } == null
