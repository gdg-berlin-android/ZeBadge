package de.berlindroid.zekompanion.server.ai

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
import java.awt.image.BufferedImage
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

private const val OPENAI_TOKEN_ENV = "DALE_AUTH_TOKEN"


@Serializable
data class ImageRequest(
    @SerialName("n") val imageCount: Int = 1,
    val size: String = "256x256",
    val model: String,
    val prompt: String,
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
        @Body request: ImageRequest,
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
    ): BufferedImage? {
        try {
            val maybeImages = service.generateImage(
                request = ImageRequest(
                    model = "dall-e-3",
                    prompt = "Please create a profile picture for a fantastical school book showing \"${name}\". " +
                            "The picture should be a decorated wooden frame hanging on a blank wall " +
                            "and show $name in a representative pose, doing their favorite thing. " +
                            "$description.",
                ),
                authorization = "Bearer $token",
            )

            val location = maybeImages.data.firstOrNull() ?: return null
            println("Avatar of '$name' generated at ${location.url}.")

            return ImageIO.read(URL(location.url))
        } catch (e: Exception) {
            e.printStackTrace()
            println("Could not generate image!")
            return null
        }
    }
}
