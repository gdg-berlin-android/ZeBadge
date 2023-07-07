package de.berlindroid.zeapp.zeui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.gson.annotations.SerializedName
import de.berlindroid.zeapp.OPENAI_API_KEY
import de.berlindroid.zeapp.PAGE_HEIGHT
import de.berlindroid.zeapp.PAGE_WIDTH
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.zebits.copy
import de.berlindroid.zeapp.zebits.cropPageFromCenter
import de.berlindroid.zeapp.zebits.isBinary
import de.berlindroid.zeapp.zebits.scaleIfNeeded
import de.berlindroid.zeapp.zemodels.ZeConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import timber.log.Timber

/**
 * Use Dall-e 2 to build a bitmap for the badge.
 *
 * @param initialPrompt the initial prompt to be used to generate an image.
 * @param dismissed callback called when the editor dialog is dismissed
 * @param accepted callback called when the image is accepted
 */
@ExperimentalMaterial3Api
@Preview
@Composable
fun ImageGenerationEditorDialog(
    initialPrompt: String = "Unicorn at an android conference in isometric view.",
    dismissed: () -> Unit = {},
    accepted: (config: ZeConfiguration.ImageGen) -> Unit = {},
    snackbarMessage: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var progress by remember { mutableStateOf<Float?>(null) }
    var prompt by remember { mutableStateOf(initialPrompt) }
    var bitmap by remember {
        mutableStateOf(
            BitmapFactory.decodeResource(
                context.resources,
                R.drawable.error,
            ).scaleIfNeeded(PAGE_WIDTH, PAGE_HEIGHT),
        )
    }

    var lastLoadedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    AlertDialog(
        onDismissRequest = dismissed,
        confirmButton = {
            Button(
                enabled = progress == null,
                onClick = {
                    if (bitmap.isBinary()) {
                        accepted(ZeConfiguration.ImageGen(prompt, bitmap))
                    } else {
                        snackbarMessage(context.getString(R.string.not_binary_image))
                    }
                },
            ) {
                Text(stringResource(id = android.R.string.ok))
            }
        },
        title = {
            Text(stringResource(id = R.string.generate_image_page))
        },
        text = {
            Column {
                BinaryImageEditor(
                    bitmap = bitmap,
                ) {
                    bitmap = it
                }

                if (progress != null) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        progress = progress!!,
                    )
                }

                TextField(
                    value = prompt,
                    enabled = progress == null,
                    singleLine = true,
                    label = { Text(text = stringResource(id = R.string.enter_prompt)) },
                    onValueChange = { prompt = it },
                )

                Button(
                    enabled = progress == null,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                progress = 0.1f
                                val generatedBitmap = requestImageGeneration(context, prompt)
                                progress = 0.8f

                                if (generatedBitmap != null) {
                                    bitmap = generatedBitmap
                                    lastLoadedBitmap = bitmap.copy()
                                } else {
                                    withContext(Dispatchers.Main) {
                                        snackbarMessage(context.getString(R.string.could_not_generate_image))
                                    }
                                    bitmap = BitmapFactory.decodeResource(
                                        context.resources,
                                        R.drawable.error,
                                    ).scaleIfNeeded(PAGE_WIDTH, PAGE_HEIGHT)
                                    lastLoadedBitmap = null
                                }
                                progress = null
                            }
                        }
                    },
                ) {
                    Text(text = stringResource(id = R.string.generate))
                }
            }
        },
    )
}

private suspend fun requestImageGeneration(
    context: Context,
    prompt: String,
): Bitmap? {
    val maybeImages = openAiApi.generateImage(
        body = OpenAIApi.GenerateImages(
            prompt = prompt,
        ),
    )

    if (maybeImages.isSuccessful) {
        val body = maybeImages.body()
        if (body != null) {
            val bitmaps = body.data.map { location ->
                val request = Request
                    .Builder()
                    .url(location.url)
                    .get()
                    .build()

                val response = ok.newCall(request).execute()
                if (response.isSuccessful) {
                    val bytes = response.body?.bytes() ?: byteArrayOf()
                    BitmapFactory.decodeByteArray(
                        bytes,
                        0,
                        bytes.size,
                    )
                } else {
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.error,
                    ).scaleIfNeeded(PAGE_WIDTH, PAGE_HEIGHT)
                }
            }

            return bitmaps.first().cropPageFromCenter()
        } else {
            Timber.e("ImageGenError", "No image returned.")
        }
    } else {
        Timber.e(
            "ImageGenError",
            "Could not fetch images: ${maybeImages.errorBody()?.string()}",
        )
    }

    return null
}

private val ok = OkHttpClient.Builder().build()

private val retrofit = Retrofit.Builder()
    .baseUrl("https://api.openai.com")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

private val openAiApi = retrofit.create(OpenAIApi::class.java)

private interface OpenAIApi {
    data class GeneratedImages(
        val created: Number,
        val data: List<ImageLocation>,
    ) {
        data class ImageLocation(
            val url: String,
        )
    }

    data class GenerateImages(
        val prompt: String,
        @SerializedName("n") val imageCount: Int = 1,
        val size: String = "512x512",
    )

    @POST("/v1/images/generations")
    suspend fun generateImage(
        @Header("Content-Type") contentType: String = "application/json",
        @Header("Authorization") authorization: String = OPENAI_API_KEY.toBearerToken(),
        @Body body: GenerateImages,
    ): Response<GeneratedImages>
}

private fun String.toBearerToken() = "Bearer $this"
