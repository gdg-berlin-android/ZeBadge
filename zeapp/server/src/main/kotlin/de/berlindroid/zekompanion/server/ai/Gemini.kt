package de.berlindroid.zekompanion.server.ai

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

private const val AI_TOKEN_ENV = "AI_AUTH_TOKEN"

@Serializable
data class PromptBody(
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
                MediaType.parse("application/json; charset=UTF8")!!,
            ),
        )
        .build()
        .create(SimpleGeminiService::class.java),
    private val token: String = System.getenv(AI_TOKEN_ENV) ?: "",
) {

    suspend fun getDescription(name: String): String {
        val prompt = PromptBody(
            contents = listOf(
                PromptBody.Content(
                    parts = listOf(
                        PromptBody.Content.Part(
                            text = "You are a dungeons and dragons dungeon master, building a dnd session at the Droidcon in Berlin in 2024. You'll answer with a 100 words description of characters backgrounds.",
                        ),
                        PromptBody.Content.Part(
                            text = "A new player joins: \"${name}\". Please give me a background description of this character playing at the Droidcon in Berlin 2024.",
                        ),
                    ),
                ),
            ),
        )

        try {
            val response = service.prompt(
                token,
                prompt,
            )

            return response.candidates.joinToString(separator = ",") { candidate ->
                candidate.content.parts.joinToString { part ->
                    part.text
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            print("Couldn't gemini!")
            return ""
        }

    }
}
