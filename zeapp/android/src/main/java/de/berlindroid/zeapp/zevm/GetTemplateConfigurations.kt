package de.berlindroid.zeapp.zevm

import android.graphics.Bitmap
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.zemodels.ZeConfiguration
import de.berlindroid.zeapp.zeservices.ZeImageProviderService
import de.berlindroid.zeapp.zeui.pixelManipulation
import de.berlindroid.zekompanion.ditherFloydSteinberg
import javax.inject.Inject

class GetTemplateConfigurations
    @Inject
    constructor(
        private val imageProviderService: ZeImageProviderService,
    ) {
        operator fun invoke(openApiKey: String): List<ZeConfiguration> {
            return mutableListOf(
                // TODO: Fetch from shared
                ZeConfiguration.Name(
                    null,
                    null,
                    imageProviderService.getInitialNameBitmap(),
                ),
                ZeConfiguration.Picture(R.drawable.soon.toBitmap()),
                // TODO: Fetch Schedule here.
                ZeConfiguration.Schedule(
                    R.drawable.soon.toBitmap(),
                ),
                ZeConfiguration.Weather(
                    "2023-07-06",
                    "26C",
                    R.drawable.soon.toBitmap(),
                ),
                ZeConfiguration.Kodee(
                    R.drawable.kodee.toBitmap().pixelManipulation { w, h -> ditherFloydSteinberg(w, h) },
                ),
                ZeConfiguration.ImageDraw(
                    R.drawable.kodee.toBitmap().pixelManipulation { w, h -> ditherFloydSteinberg(w, h) },
                ),
                ZeConfiguration.Camera(R.drawable.soon.toBitmap().pixelManipulation { w, h -> ditherFloydSteinberg(w, h) }),
                ZeConfiguration.Camera(R.drawable.soon.toBitmap().pixelManipulation { w, h -> ditherFloydSteinberg(w, h) }),
                ZeConfiguration.CustomPhrase(
                    "Custom phrase",
                    R.drawable.page_phrase.toBitmap().pixelManipulation { w, h -> ditherFloydSteinberg(w, h) },
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
