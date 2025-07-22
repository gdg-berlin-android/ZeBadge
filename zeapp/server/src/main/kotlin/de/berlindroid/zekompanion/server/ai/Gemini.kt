package de.berlindroid.zekompanion.server.ai

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

private const val AI_TOKEN_ENV = "AI_AUTH_TOKEN"

@Serializable
data class PromptBody(
    val systemInstruction: Content,
    val contents: List<Content>,
) {
    @Serializable
    data class Content(
        val parts: List<Part>,
    ) {
        @Serializable
        data class Part(
            val text: String,
        )
    }
}

@Serializable
data class PromptResponse(
    val candidates: List<Candidate>,
) {
    @Serializable
    data class Candidate(
        val content: Content,
        val finishReason: String,
        val index: Int,
    ) {
        @Serializable
        data class Content(
            val parts: List<Part>,
            val role: String,
        ) {
            @Serializable
            data class Part(
                val text: String,
            )
        }
    }
}

interface SimpleGeminiService {
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun prompt(
        @Query("key") key: String,
        @Body prompt: PromptBody,
    ): PromptResponse
}


class Gemini(
    private val json: Json = Json { ignoreUnknownKeys = true },
    private val service: SimpleGeminiService = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .addConverterFactory(
            json.asConverterFactory(
                "application/json; charset=UTF8".toMediaType(),
            ),
        )
        .build()
        .create(SimpleGeminiService::class.java),
    private val token: String = System.getenv(AI_TOKEN_ENV) ?: "",
) {

    suspend fun getDescription(name: String): String {
        return geminiIt(
            systemInstruction = "You are a dungeons and dragons dungeon master, assembling a new dnd campaign " +
                    "at the Droidcon in Berlin conference in 2024. You'll answer following questions with a " +
                    "description of a character's background story. Their all Android developers, either " +
                    "excited new comers or old hands with years of experience. Please keep it brief, " +
                    "interesting and quirky.",
            prompts = listOf(
                "Please give \"${name}\" a background story of their character.",
            ),
        )
    }

    suspend fun getChatPhrase(name: String, description: String): String {
        return geminiIt(
            systemInstruction = "You are a one word wonder. Answer the following prompts with one word.",
            prompts = listOf(
                "Hey, can you create a one word catchphrase for $name who is described as $description.",
            ),
        )
    }

    private suspend fun geminiIt(prompts: List<String>, systemInstruction: String = ""): String {
        try {
            val response = service.prompt(
                token,
                PromptBody(
                    systemInstruction =
                    PromptBody.Content(
                        parts = listOf(
                            PromptBody.Content.Part(
                                text = systemInstruction,
                            ),
                        ),
                    ),
                    contents = listOf(
                        PromptBody.Content(
                            parts = prompts.map {
                                PromptBody.Content.Part(
                                    text = it,
                                )
                            },
                        ),
                    ),
                ),
            )

            return response.candidates.joinToString(separator = ",") { candidate ->
                candidate.content.parts.joinToString { part ->
                    part.text.replace(Regex("[_*#\n]"), "").trim()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            print("Couldn't gemini!")
            return ""
        }
    }
}
