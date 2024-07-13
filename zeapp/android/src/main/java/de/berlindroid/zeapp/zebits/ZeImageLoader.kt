package de.berlindroid.zeapp.zebits

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import com.commit451.coiltransformations.CropTransformation
import de.berlindroid.zeapp.zeui.pixelManipulation
import de.berlindroid.zekompanion.BADGE_HEIGHT
import de.berlindroid.zekompanion.BADGE_WIDTH
import de.berlindroid.zekompanion.ditherFloydSteinberg

suspend fun Uri.toDitheredImage(context: Context): Bitmap {
    val imageRequest =
        ImageRequest.Builder(context)
            .data(this)
            .transformations(CropTransformation())
            .size(BADGE_WIDTH, BADGE_HEIGHT)
            .scale(Scale.FIT)
            .precision(Precision.EXACT)
            .allowHardware(false)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .diskCachePolicy(CachePolicy.DISABLED)
            .build()

    val drawable = context.imageLoader.execute(imageRequest).drawable as BitmapDrawable
    val bitmap =
        Bitmap.createBitmap(
            BADGE_WIDTH,
            BADGE_HEIGHT,
            Bitmap.Config.ARGB_8888,
        )
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)
    canvas.drawBitmap(
        drawable.bitmap,
        (BADGE_WIDTH / 2f) - (drawable.bitmap.width / 2f),
        0f,
        null,
    )

    return bitmap.pixelManipulation { w, h -> ditherFloydSteinberg(w, h) }
}
