package de.berlindroid.zeapp.zeservices

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import dagger.hilt.android.qualifiers.ApplicationContext
import de.berlindroid.zeapp.PAGE_HEIGHT
import de.berlindroid.zeapp.PAGE_WIDTH
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.zebits.scaleIfNeeded
import javax.inject.Inject

class ZeImageProviderService @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun getInitialNameBitmap(): Bitmap {
        return BitmapFactory.decodeResource(
            context.resources,
            R.drawable.sample_badge,
        ).scaleIfNeeded(PAGE_WIDTH, PAGE_HEIGHT)
    }

    fun provideImageBitmap(@DrawableRes imageResource: Int): Bitmap {
        return BitmapFactory.decodeResource(
            context.resources,
            imageResource,
            BitmapFactory.Options().apply { inScaled = false },
        ).scaleIfNeeded(PAGE_WIDTH, PAGE_HEIGHT)
    }
}
