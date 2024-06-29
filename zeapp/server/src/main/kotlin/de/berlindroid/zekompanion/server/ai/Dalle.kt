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

const val USER_PROFILE_PICTURE_SIZE = 32

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
                request = ImageRequest(
                    model = "dall-e-3",
                    prompt = "Please create a digital picture of \"${name}\", a player character of a black and white pixelated game. " +
                            "The picture should show them in action doing their favorite thing, it should be isometric. " +
                            "$name can be described as follows: '${description}'.",
                ),
                authorization = "Bearer $token",
            )

            val location = maybeImages.data.firstOrNull() ?: return null
            println("Avatar of '$name' generated at ${location.url}.")

            val image = ImageIO.read(URL(location.url))
            val width = image.width
            val height = image.height
            val b64 = image
                .toPixels()
                .resize(width, height, USER_PROFILE_PICTURE_SIZE, USER_PROFILE_PICTURE_SIZE)
                .ditherFloydSteinberg(USER_PROFILE_PICTURE_SIZE, USER_PROFILE_PICTURE_SIZE)
                .toBinary()
                .zipit()
                .base64()

            return b64
        } catch (e: Exception) {
            e.printStackTrace()
            println("Could not generate image!")
            return null
        }
    }
}
