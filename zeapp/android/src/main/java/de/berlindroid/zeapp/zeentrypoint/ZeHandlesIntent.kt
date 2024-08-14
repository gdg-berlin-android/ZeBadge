package de.berlindroid.zeapp.zeentrypoint

import android.content.Intent
import android.net.Uri
import androidx.core.content.IntentCompat
import androidx.lifecycle.lifecycleScope
import de.berlindroid.zeapp.zebits.toDitheredImage
import de.berlindroid.zeapp.zemodels.ZeConfiguration
import de.berlindroid.zeapp.zemodels.ZeSlot
import kotlinx.coroutines.launch

fun ZeMainActivity.handleIntent() {
    if (intent.type?.startsWith("image/") == true) {
        handleSendImage(intent)
    }
}

private fun ZeMainActivity.handleSendImage(intent: Intent) {
    IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
        ?.let(::updateSelectedImage)
}

private fun ZeMainActivity.updateSelectedImage(imageUri: Uri) =
    lifecycleScope.launch {
        val bitmap = imageUri.toDitheredImage(this@updateSelectedImage)
        vm.slotConfigured(
            ZeSlot.Camera,
            ZeConfiguration.Camera(bitmap),
        )
    }
