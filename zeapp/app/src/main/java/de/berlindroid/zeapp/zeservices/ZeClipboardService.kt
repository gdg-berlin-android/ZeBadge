package de.berlindroid.zeapp.zeservices

import android.content.ClipData
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.berlindroid.zeapp.R
import javax.inject.Inject

class ZeClipboardService @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun copyToClipboard(text: String) {
        val manager = context.getSystemService(android.content.ClipboardManager::class.java)
        manager.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.app_name), text))
    }
}
