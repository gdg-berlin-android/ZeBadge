package de.berlindroid.zekompanion.server.ai

import java.lang.IndexOutOfBoundsException


class AI(
    private val gemini: Gemini = Gemini(),
    private val dale: Dalle = Dalle(),
    private val firstNames: MutableList<String> = mutableListOf(),
    private val lastNames: MutableList<String> = mutableListOf(),
    private val prefixes: List<String> = listOf(
        "Al",
        "Bint",
        "Ibn",
        "Mac",
        "Mc",
        "Nic",
        "Ní",
        "da ",
        "de ",
        "di ",
        "le ",
        "van ",
        "von ",
    ),
) {
    init {
        val names = this.javaClass.classLoader.getResource("names.txt")?.readText()
        names?.lines()?.forEachIndexed { index, name ->
            try {
                val (first, last) = name.split(" ")
                if (first !in firstNames) {
                    firstNames += first
                }

                if (last !in lastNames) {
                    lastNames += last
                }
            } catch (e: IndexOutOfBoundsException) {
                println("Couldn't split name '${name}' at index ${index}. Ignoring it.")
            }
        }
    }

    suspend fun createUserName(): String = "${firstNames.random()} ${createMaybeRandomPrefix()}${lastNames.random()}"

    private fun createMaybeRandomPrefix(): String = when (Math.random()) {
        in 0.95..1.0 -> prefixes.random()
        else -> ""
    }

    suspend fun createUserDescription(name: String): String = gemini.getDescription(name)

    suspend fun createUserImage(name: String, description: String): String = dale.requestImageGeneration(
        name = name,
        description = description,
    ) ?: ""
}
