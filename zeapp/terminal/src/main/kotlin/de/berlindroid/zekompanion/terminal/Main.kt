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
import kotlinx.coroutines.runBlocking
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.File
import java.nio.IntBuffer
import javax.imageio.ImageIO
import kotlin.system.exitProcess

const val COLOR_RED_BACKGROUND = "\u001B[41m"
const val COLOR_GREEN_BACKGROUND = "\u001B[42m"
const val COLOR_END = "\u001B[m"

data class CommandLineArgument(
    val name: String,
    val description: String,
    val short: String,
    val long: String,
    val hasParameter: Boolean = false,
    val callback: (it: String?) -> Unit,
)

typealias Operation = IntBuffer.(width: Int, height: Int) -> IntBuffer

data object Configuration {
    var input: String? = null
    var output: String? = null
    var width: Int = 0
    var height: Int = 0
    var operations: MutableList<Pair<String, Operation>> = mutableListOf()
}

fun inputOutputCommands() = mutableListOf(
    CommandLineArgument(
        name = "input image",
        description = "Image to be manipulated",
        short = "-i",
        long = "--input",
        hasParameter = true,
    ) { Configuration.input = it },
    CommandLineArgument(
        name = "output image",
        description = "Image manipulation results will get stored here",
        short = "-o",
        long = "--output",
        hasParameter = true,
    ) { Configuration.output = it },
    CommandLineArgument(
        name = "send image to badge",
        description = "Sends the hopefully converted image to the hopefully connected badge.",
        short = "-s",
        long = "--send",
    ) {
        Configuration.operations.add(
            "send" to sendBufferToBadge(),
        )
    },
)

fun imageColorCommands() = listOf(
    CommandLineArgument(
        name = "dither floyd-steinberg",
        description = "Converts the given input image into a black and white output image, somewhat preserving the luminance using dithering.",
        short = "-fs",
        long = "--floyd-steinberg",
    ) { Configuration.operations.add("fs dither" to IntBuffer::ditherFloydSteinberg) },
    CommandLineArgument(
        name = "image threshold",
        description = "Thresholds the given input image. Value above the parameter are considered white, others black",
        short = "-t",
        long = "--threshold",
    ) { Configuration.operations.add("threshold" to { _, _ -> threshold() }) },
    CommandLineArgument(
        name = "image inversion",
        description = "Turn black into white and opposite.",
        short = "-i",
        long = "--invert",
    ) { Configuration.operations.add("invert" to { _, _ -> invert() }) },
    CommandLineArgument(
        name = "grayscale an image",
        description = "Convert image into a smooth transition from black to white color.",
        short = "-g",
        long = "--grayscale",
    ) { Configuration.operations.add("gray" to { _, _ -> grayscale() }) },
)

fun resizeCommands() = listOf(
    CommandLineArgument(
        name = "resize image",
        description = "Simple resize of image into 'WIDTHxHEIGHT' image.",
        short = "-r",
        long = "--resize",
        hasParameter = true,
    ) { Configuration.operations.add("resize" to resizeImageCallback(it)) },
    CommandLineArgument(
        name = "fulidly resize image",
        description = "Simple fluidly resize of image into 'WIDTHxHEIGHT' image.",
        short = "-f",
        long = "--fluid",
        hasParameter = true,
    ) { Configuration.operations.add("fluid" to resizeFluidImageCallback(it)) },
)

fun helpCommand() = CommandLineArgument(
    name = "help",
    description = "Helps you",
    short = "-h",
    long = "--help",
) { help() }

val commands: List<CommandLineArgument> = listOf(
    helpCommand(),
    *inputOutputCommands().toTypedArray(),
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

private fun sendBufferToBadge(): IntBuffer.(width: Int, height: Int) -> IntBuffer = { _, _ ->
    val payload = BadgePayload(
        debug = false,
        type = "preview",
        meta = "",
        payload = toBinary().zipit().base64(),
    )

    runBlocking {
        with(buildBadgeManager("")) {
            if (isConnected()) {
                val result = sendPayload(payload)
                if (result.isSuccess) {
                    println("${COLOR_GREEN_BACKGROUND}Successfully sent image (${result.getOrNull()}).${COLOR_END}")
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
            println("${COLOR_RED_BACKGROUND}Command for argument '$argument' not found.${COLOR_END}\nTry '--help' for general help.")
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
    if (!Configuration.input.isNullOrEmpty()) {
        if (neitherOutputNorSend()) {
            println("${COLOR_RED_BACKGROUND}Output (-o/--output) not set. Image manipulation impossible, exiting.${COLOR_END}")
        } else {
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

            Configuration.operations.forEach { operation ->
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
    }
}

private fun neitherOutputNorSend() =
    Configuration.output == null
            && Configuration.operations.firstOrNull { it.first == "send" } == null
