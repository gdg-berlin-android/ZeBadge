package de.berlindroid.zeapp.zevm

import android.graphics.Bitmap
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.zebits.ditherFloydSteinberg
import de.berlindroid.zeapp.zemodels.ZeConfiguration
import de.berlindroid.zeapp.zeservices.ZeImageProviderService
import javax.inject.Inject

class GetTemplateConfigurations @Inject constructor(
    private val imageProviderService: ZeImageProviderService,
) {

    operator fun invoke(openApiKey: String): List<ZeConfiguration> {
        return mutableListOf(
            ZeConfiguration.Name(
                null,
                null,
                imageProviderService.getInitialNameBitmap(),
            ), // TODO: Fetch from shared

            ZeConfiguration.Picture(R.drawable.soon.toBitmap()),

            ZeConfiguration.Schedule(
                R.drawable.soon.toBitmap(),
            ), // TODO: Fetch Schedule here.

            ZeConfiguration.Weather(
                "2023-07-06",
                "26C",
                R.drawable.soon.toBitmap(),
            ),

            ZeConfiguration.Kodee(
                R.drawable.kodee.toBitmap().ditherFloydSteinberg(),
            ),
            ZeConfiguration.ImageDraw(
                R.drawable.kodee.toBitmap().ditherFloydSteinberg(),
            ),
            ZeConfiguration.Camera(R.drawable.soon.toBitmap().ditherFloydSteinberg()),
            ZeConfiguration.Camera(R.drawable.soon.toBitmap().ditherFloydSteinberg()),
            ZeConfiguration.CustomPhrase(
                "Custom phrase",
                R.drawable.page_phrase.toBitmap().ditherFloydSteinberg(),
            ),
        ).apply {
            // Surprise mechanic: If token is set, show open ai item
            if (openApiKey.isNotBlank()) {
                add(
                    2,
                    ZeConfiguration
                        .ImageGen(
                            prompt = "An Android developer at a conference in Berlin.",
                            bitmap = R.drawable.soon.toBitmap(),
                        ),
                )
            }
        }
    }

    private fun Int.toBitmap(): Bitmap {
        return imageProviderService.provideImageBitmap(this)
    }
}
