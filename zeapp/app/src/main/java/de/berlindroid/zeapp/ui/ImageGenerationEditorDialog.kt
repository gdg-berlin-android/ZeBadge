package de.berlindroid.zeapp.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.scale
import com.google.gson.annotations.SerializedName
import de.berlindroid.zeapp.OPENAI_API_KEY
import de.berlindroid.zeapp.PAGE_HEIGHT
import de.berlindroid.zeapp.PAGE_WIDTH
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.bits.copy
import de.berlindroid.zeapp.bits.cropPageFromCenter
import de.berlindroid.zeapp.bits.isBinary
import de.berlindroid.zeapp.bits.scaleIfNeeded
import de.berlindroid.zeapp.vm.BadgeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST


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
    accepted: (config: BadgeViewModel.Configuration.ImageGen) -> Unit = {},
) {
    val context = LocalContext.current

    var progress by remember { mutableStateOf<Float?>(null) }
    var prompt by remember { mutableStateOf(initialPrompt) }
    var bitmap by remember {
        mutableStateOf(
            BitmapFactory.decodeResource(
                context.resources,
                R.drawable.error,
            ).scaleIfNeeded(PAGE_WIDTH, PAGE_HEIGHT)
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
                        accepted(BadgeViewModel.Configuration.ImageGen(prompt, bitmap))
                    } else {
                        Toast.makeText(context, "Not a binary image.", Toast.LENGTH_LONG).show()
                    }
                }) {
                Text(stringResource(id = android.R.string.ok))
            }
        },
        title = {
            Text("Generate Image Page")
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
                        progress = progress!!
                    )
                }

                TextField(
                    value = prompt,
                    enabled = progress == null,
                    singleLine = true,
                    label = { Text(text = "Enter your prompt here") },
                    onValueChange = { prompt = it }
                )

                Button(
                    enabled = progress == null,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            progress = 0.1f
                            val generatedBitmap = requestImageGeneration(context, prompt)
                            progress = 0.8f

                            if (generatedBitmap != null) {
                                bitmap = generatedBitmap
                                lastLoadedBitmap = bitmap.copy()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Could not generate an image",
                                    Toast.LENGTH_LONG
                                ).show()

                                bitmap = BitmapFactory.decodeResource(
                                    context.resources,
                                    R.drawable.error
                                ).scaleIfNeeded(PAGE_WIDTH, PAGE_HEIGHT)
                                lastLoadedBitmap = null
                            }
                            progress = null
                        }
                    }
                ) {
                    Text(text = "Generate")
                }
            }
        }
    )
}

private suspend fun requestImageGeneration(
    context: Context,
    prompt: String,
): Bitmap? {
    val maybeImages = openAiApi.generateImage(
        body = OpenAIApi.GenerateImages(
            prompt = prompt,
        )
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
                    val bytes = response.body()?.bytes() ?: byteArrayOf()
                    BitmapFactory.decodeByteArray(
                        bytes,
                        0,
                        bytes.size
                    )
                } else {
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.error
                    ).scaleIfNeeded(PAGE_WIDTH, PAGE_HEIGHT)
                }
            }

            return bitmaps.first().cropPageFromCenter()
        } else {
            Log.e("ImageGenError", "No image returned.")
        }
    } else {
        Log.e(
            "ImageGenError",
            "Could not fetch images: ${maybeImages.errorBody()?.string()}"
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
            val url: String
        )
    }

    data class GenerateImages(
        val prompt: String,
        @SerializedName("n") val imageCount: Int = 1,
        val size: String = "512x512"
    )

    @POST("/v1/images/generations")
    suspend fun generateImage(
        @Header("Content-Type") contentType: String = "application/json",
        @Header("Authorization") authorization: String = OPENAI_API_KEY.toBearerToken(),
        @Body body: GenerateImages
    ): Response<GeneratedImages>
}

private fun String.toBearerToken() = "Bearer $this"