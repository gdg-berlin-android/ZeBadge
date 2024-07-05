package de.berlindroid.zeapp.zeservices

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import dagger.hilt.android.qualifiers.ApplicationContext
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.zebits.scaleIfNeeded
import de.berlindroid.zekompanion.BADGE_HEIGHT
import de.berlindroid.zekompanion.BADGE_WIDTH
import javax.inject.Inject

class ZeImageProviderService
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun getInitialNameBitmap(): Bitmap {
            return BitmapFactory.decodeResource(
                context.resources,
                R.drawable.sample_badge,
            ).scaleIfNeeded(BADGE_WIDTH, BADGE_HEIGHT)
        }

        fun provideImageBitmap(
            @DrawableRes imageResource: Int,
        ): Bitmap {
            return BitmapFactory.decodeResource(
                context.resources,
                imageResource,
                BitmapFactory.Options().apply { inScaled = false },
            ).scaleIfNeeded(BADGE_WIDTH, BADGE_HEIGHT)
        }
    }
