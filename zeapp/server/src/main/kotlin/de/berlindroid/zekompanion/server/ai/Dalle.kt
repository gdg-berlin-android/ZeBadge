package de.berlindroid.zekompanion.server.ai

import de.berlindroid.zekompanion.*
import de.berlindroid.zekompanion.server.ext.ImageExt.toPixels
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

private const val OPENAI_TOKEN_ENV = "DALE_AUTH_TOKEN"


@Serializable
data class ImagePrompt(
    val prompt: String,
    @SerialName("n") val imageCount: Int = 1,
    val size: String = "256x256",
)

@Serializable
data class GeneratedImages(
    val created: Int,
    val data: List<ImageLocation>,
) {
    @Serializable
    data class ImageLocation(
        val url: String,
    )
}

interface OpenAIService {
    @POST("/v1/images/generations")
    suspend fun generateImage(
        @Header("Content-Type") contentType: String = "application/json",
        @Header("Authorization") authorization: String,
        @Body prompt: ImagePrompt,
    ): GeneratedImages
}

private const val TIMEOUT: Long = 90

class Dalle(
    private val json: Json = Json { ignoreUnknownKeys = true },
    private val service: OpenAIService = Retrofit.Builder()
        .baseUrl("https://api.openai.com/")
        .addConverterFactory(
            json.asConverterFactory(
                MediaType.parse("application/json; charset=UTF8")!!,
            ),
        )
        .client(
            OkHttpClient().newBuilder()
                .callTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .build(),
        )
        .build()
        .create(OpenAIService::class.java),

    private val token: String = System.getenv(OPENAI_TOKEN_ENV) ?: ("" + println("OAI token not found!")),
) {

    suspend fun requestImageGeneration(
        name: String,
        description: String,
    ): String? {
        try {
            val maybeImages = service.generateImage(
                prompt = ImagePrompt(
                    prompt = "Please draw me a black and white picture of \"${name}\". " +
                            "They can be  described as follows: ${description}. Thank you.",
                ),
                authorization = "Bearer $token",
            )

            val location = maybeImages.data.firstOrNull() ?: return null

            val image = ImageIO.read(URL(location.url))
            val width = image.width
            val height = image.height
            val pixels = image
                .toPixels()
                .resize(width, height, 32, 32)
                .ditherFloydSteinberg(32, 32)
                .toBinary()
                .zipit()
                .base64()

            return pixels
        } catch (e: Exception) {
            e.printStackTrace()
            println("Could not generate image!")
            return null
        }
    }
}
